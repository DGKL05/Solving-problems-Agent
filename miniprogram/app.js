App({
  globalData: {
    userInfo: null,
    token: null,
    userId: null,
    baseUrl: 'https://your-api.domain.com'
  },

  onLaunch() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
      this.globalData.userId = wx.getStorageSync('userId');
    }
  }
});
