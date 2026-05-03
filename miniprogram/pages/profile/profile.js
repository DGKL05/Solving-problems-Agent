Page({
  data: {
    userInfo: null,
    avatarChar: '👤',
    stats: { totalSolved: 0, mistakes: 0, sessions: 0 }
  },

  onShow: function() {
    var token = wx.getStorageSync('token');
    var userId = wx.getStorageSync('userId');
    if (token && userId) {
      var nickname = wx.getStorageSync('nickname') || ('用户' + userId);
      var avatarUrl = wx.getStorageSync('avatarUrl') || '';
      this.setData({
        userInfo: { nickname: nickname, userId: userId, avatarUrl: avatarUrl },
        avatarChar: nickname.charAt(0)
      });
    } else {
      this.setData({ userInfo: null, avatarChar: '👤' });
    }
  },

  onLogin: function() {
    var that = this;
    wx.showLoading({ title: '登录中...' });

    wx.login({
      success: function(res) {
        if (!res.code) { wx.hideLoading(); wx.showToast({ title: '登录失败', icon: 'none' }); return; }
        wx.request({
          url: getApp().globalData.baseUrl + '/api/auth/login',
          method: 'POST',
          data: { code: res.code },
          success: function(resp) {
            var body = resp.data;
            if (typeof body === 'string') body = JSON.parse(body);
            wx.hideLoading();
            if (body.token) {
              wx.setStorageSync('token', body.token);
              wx.setStorageSync('userId', body.userId);
              getApp().globalData.token = body.token;
              getApp().globalData.userId = body.userId;
              var nickname = '用户' + body.userId;
              wx.setStorageSync('nickname', nickname);
              that.setData({
                userInfo: { nickname: nickname, userId: body.userId, avatarUrl: '' },
                avatarChar: nickname.charAt(0)
              });
              wx.showToast({ title: '登录成功', icon: 'success' });
            } else {
              wx.showToast({ title: '登录失败', icon: 'none' });
            }
          },
          fail: function() { wx.hideLoading(); wx.showToast({ title: '网络错误', icon: 'none' }); }
        });
      },
      fail: function() { wx.hideLoading(); wx.showToast({ title: '微信登录失败', icon: 'none' }); }
    });
  },

  onNickFocus: function(e) {
    console.log('[Profile] nickname focus');
  },

  onChooseAvatar: function(e) {
    var avatarUrl = e.detail.avatarUrl;
    if (!avatarUrl) return;
    var userInfo = this.data.userInfo;
    userInfo.avatarUrl = avatarUrl;
    this.setData({ userInfo: userInfo });
    wx.setStorageSync('avatarUrl', avatarUrl);
    // Auto focus nickname after avatar chosen
    setTimeout(function() { this.setData({ nickFocus: true }); }.bind(this), 300);
  },

  onNickFocus: function(e) {
    console.log('[Profile] nickname focus');
  },

  onNicknameBlur: function(e) {
    var nickname = e.detail.value;
    if (!nickname) return;
    var userInfo = this.data.userInfo;
    userInfo.nickname = nickname;
    this.setData({ userInfo: userInfo, avatarChar: nickname.charAt(0), nickFocus: false });
    wx.setStorageSync('nickname', nickname);
  },

  onClearCache: function() {
    wx.clearStorageSync();
    this.setData({ userInfo: null, avatarChar: '👤' });
    wx.showToast({ title: '已清除', icon: 'success' });
  }
});
