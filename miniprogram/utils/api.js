var app = getApp();

function getToken() {
  return app.globalData.token || wx.getStorageSync('token');
}

function doLogin(callback) {
  wx.login({
    success(res) {
      if (!res.code) { return; }
      wx.request({
        url: app.globalData.baseUrl + '/api/auth/login',
        method: 'POST',
        data: { code: res.code },
        success(resp) {
          var body = resp.data;
          if (typeof body === 'string') body = JSON.parse(body);
          // AuthController returns LoginResponse directly (token, userId, etc.)
          if (body.token) {
            wx.setStorageSync('token', body.token);
            wx.setStorageSync('userId', body.userId);
            app.globalData.token = body.token;
            app.globalData.userId = body.userId;
            if (callback) callback();
          }
        }
      });
    }
  });
}

function request(options) {
  var token = getToken();

  return new Promise(function(resolve, reject) {
    wx.request({
      url: app.globalData.baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json'
      },
      success: function(res) {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else if (res.statusCode === 401) {
          doLogin(function() {
            request(options).then(resolve, reject);
          });
        } else {
          reject(res.data);
        }
      },
      fail: function(err) { reject(err); }
    });
  });
}

function doUpload(filePath, subjectType, sessionId, resolve, reject) {
  var token = getToken();
  if (!token) {
    doLogin(function() { doUpload(filePath, subjectType, sessionId, resolve, reject); });
    return;
  }

  wx.uploadFile({
    url: app.globalData.baseUrl + '/api/chat/upload?token=' + token,
    filePath: filePath,
    name: 'file',
    formData: {
      subjectType: subjectType,
      sessionId: sessionId
    },
    header: {
      'Authorization': 'Bearer ' + token
    },
    success: function(res) {
      try {
        var data = JSON.parse(res.data);
        if (data.code === 401) {
          doLogin(function() { doUpload(filePath, subjectType, sessionId, resolve, reject); });
        } else {
          resolve(data);
        }
      } catch (e) {
        reject(new Error('解析服务器响应失败'));
      }
    },
    fail: function(err) { reject(err); }
  });
}

function upload(filePath, subjectType, sessionId) {
  return new Promise(function(resolve, reject) {
    doUpload(filePath, subjectType, sessionId, resolve, reject);
  });
}

module.exports = { request: request, upload: upload };
