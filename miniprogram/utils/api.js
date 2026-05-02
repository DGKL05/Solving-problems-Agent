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

function upload(filePath, subjectType, sessionId) {
  const token = auth.getToken();

  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: `${app.globalData.baseUrl}/api/chat/upload`,
      filePath: filePath,
      name: 'file',
      formData: {
        subjectType: subjectType,
        sessionId: sessionId
      },
      header: {
        'Authorization': `Bearer ${token}`
      },
      success(res) {
        resolve(JSON.parse(res.data));
      },
      fail(err) {
        reject(err);
      }
    });
  });
}

module.exports = { request, upload };
