const auth = require('../../utils/auth');

Page({
  data: {
    userInfo: null,
    stats: {
      totalSolved: 0,
      mistakes: 0,
      sessions: 0
    }
  },

  onShow() {
    const token = auth.getToken();
    if (token) {
      this.setData({
        userInfo: {
          nickname: '学生用户',
          avatarUrl: ''
        }
      });
    }
  },

  onLogin() {
    auth.login((data) => {
      this.setData({
        userInfo: {
          nickname: data.nickname || '学生用户',
          avatarUrl: data.avatarUrl || ''
        }
      });
    });
  },

  onClearCache() {
    wx.clearStorageSync();
    this.setData({ userInfo: null });
    wx.showToast({ title: '缓存已清除', icon: 'success' });
  }
});
