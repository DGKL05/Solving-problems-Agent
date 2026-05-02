const app = getApp();
const auth = require('./auth');

function request(options) {
  const token = auth.getToken();

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.baseUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      success(res) {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else if (res.statusCode === 401) {
          auth.login(() => {
            request(options).then(resolve).catch(reject);
          });
        } else {
          reject(res.data);
        }
      },
      fail(err) {
        reject(err);
      }
    });
  });
}

function doUpload(filePath, subjectType, sessionId, resolve, reject) {
  const token = auth.getToken();
  if (!token) {
    auth.login(() => doUpload(filePath, subjectType, sessionId, resolve, reject));
    return;
  }

  wx.uploadFile({
    url: `${app.globalData.baseUrl}/api/chat/upload`,
    filePath: filePath,
    name: 'file',
    formData: {
      token: token,
      subjectType: subjectType,
      sessionId: sessionId
    },
    header: {
      'Authorization': `Bearer ${token}`
    },
    success(res) {
      try {
        const data = JSON.parse(res.data);
        if (data.code === 401) {
          auth.login(() => doUpload(filePath, subjectType, sessionId, resolve, reject));
        } else {
          resolve(data);
        }
      } catch (e) {
        reject(new Error('解析服务器响应失败: ' + (res.data || '').substring(0, 100)));
      }
    },
    fail(err) {
      reject(err);
    }
  });
}

function upload(filePath, subjectType, sessionId) {
  return new Promise((resolve, reject) => {
    doUpload(filePath, subjectType, sessionId, resolve, reject);
  });
}

module.exports = { request, upload };
