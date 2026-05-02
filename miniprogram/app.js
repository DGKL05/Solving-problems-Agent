App({
  globalData: {
    userInfo: null,
    token: null,
    userId: null,
    baseUrl: 'http://localhost:8080'
  },

  onLaunch() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
      this.globalData.userId = wx.getStorageSync('userId');
    }
  }
});
