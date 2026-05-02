const app = getApp();

function login(callback) {
  wx.login({
    success(res) {
      if (res.code) {
        wx.request({
          url: `${app.globalData.baseUrl}/api/auth/login`,
          method: 'POST',
          data: { code: res.code },
          success(resp) {
            if (resp.data.code === 200) {
              const { token, userId, nickname, avatarUrl } = resp.data.data;
              app.globalData.token = token;
              app.globalData.userId = userId;
              wx.setStorageSync('token', token);
              wx.setStorageSync('userId', userId);
              if (callback) callback(resp.data.data);
            }
          }
        });
      }
    }
  });
}

function getToken() {
  return app.globalData.token || wx.getStorageSync('token');
}

function getUserId() {
  return app.globalData.userId || wx.getStorageSync('userId');
}

module.exports = { login, getToken, getUserId };
