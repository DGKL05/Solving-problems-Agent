var app = getApp();

function login(callback) {
  console.log('[Auth] login() called');
  wx.login({
    success(res) {
      console.log('[Auth] wx.login success, code:', res.code ? res.code.substring(0,10)+'...' : 'null');
      if (res.code) {
        wx.request({
          url: app.globalData.baseUrl + '/api/auth/login',
          method: 'POST',
          data: { code: res.code },
          success(resp) {
            var body = resp.data;
            if (typeof body === 'string') { body = JSON.parse(body); }
            console.log('[Auth] request success, code:', body.code);
            if (body.code === 200) {
              var data = body.data;
              app.globalData.token = data.token;
              app.globalData.userId = data.userId;
              wx.setStorageSync('token', data.token);
              wx.setStorageSync('userId', data.userId);
              console.log('[Auth] login OK, userId:', data.userId);
              if (callback) callback(data);
            } else {
              var msg = '登录失败: ' + (body.message || '未知错误');
              console.error('[Auth]', msg);
              wx.showToast({ title: msg, icon: 'none', duration: 3000 });
            }
          },
          fail(err) {
            console.error('[Auth] request fail:', err);
            wx.showToast({ title: '网络错误: ' + app.globalData.baseUrl, icon: 'none', duration: 3000 });
          }
        });
      } else {
        console.error('[Auth] wx.login no code');
        wx.showToast({ title: '微信登录失败(无code)', icon: 'none', duration: 3000 });
      }
    },
    fail(err) {
      console.error('[Auth] wx.login fail:', err);
      wx.showToast({ title: '微信授权失败', icon: 'none', duration: 3000 });
    }
  });
}

function getToken() {
  return app.globalData.token || wx.getStorageSync('token');
}

function getUserId() {
  return app.globalData.userId || wx.getStorageSync('userId');
}

module.exports = { login: login, getToken: getToken, getUserId: getUserId };
