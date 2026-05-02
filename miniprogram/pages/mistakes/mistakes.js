const api = require('../../utils/api');
const auth = require('../../utils/auth');

Page({
  data: {
    mistakes: [],
    tags: [],
    activeTag: null
  },

  onShow() {
    this.loadMistakes();
  },

  loadMistakes() {
    auth.login(() => {
      wx.showLoading({ title: '加载中...' });
      // Fetch mistakes from API
      this.setData({
        mistakes: [
          { id: 1, problemId: 1, subjectType: 'ACM', cleanedText: '给定一个数组，找出最大子数组和...', errorType: '思路错误', memo: '忘了Kadane算法', tags: ['DP', '数组'], createdAt: '2026-05-01' }
        ]
      });
      wx.hideLoading();
    });
  },

  onTagFilter(e) {
    const tag = e.currentTarget.dataset.tag;
    this.setData({ activeTag: tag === this.data.activeTag ? null : tag });
  },

  viewDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.showToast({ title: '查看题目 #' + id, icon: 'none' });
  }
});
