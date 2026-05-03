const api = require('../../utils/api');
const websocket = require('../../utils/websocket');

function doLogin(callback) {
  wx.login({
    success(res) {
      if (!res.code) { wx.showToast({ title: '登录失败', icon: 'none' }); return; }
      wx.request({
        url: getApp().globalData.baseUrl + '/api/auth/login',
        method: 'POST',
        data: { code: res.code },
        success(resp) {
          var body = resp.data;
          if (typeof body === 'string') body = JSON.parse(body);
          // AuthController returns LoginResponse directly
          if (body.token) {
            wx.setStorageSync('token', body.token);
            wx.setStorageSync('userId', body.userId);
            getApp().globalData.token = body.token;
            getApp().globalData.userId = body.userId;
            if (callback) callback();
          }
        },
        fail() { wx.showToast({ title: '网络错误', icon: 'none' }); }
      });
    }
  });
}

Page({
  data: {
    messages: [],
    inputText: '',
    sessionId: null,
    showSubjectPicker: false,
    pendingImagePath: null
  },

  onLoad() {
    doLogin(() => {
      websocket.connect((msg) => {
        if (msg.type === 'connected') {
          this.setData({ sessionId: msg.sessionId });
        } else if (msg.type === 'message') {
          const messages = this.data.messages;
          messages.push({ role: 'assistant', content: msg.content, time: new Date() });
          this.setData({ messages });
        }
      });
    });
  },

  onUnload() {
    websocket.close();
  },

  onInput(e) {
    this.setData({ inputText: e.detail.value });
  },

  sendMessage() {
    const text = this.data.inputText.trim();
    if (!text) return;

    const messages = this.data.messages;
    messages.push({ role: 'user', content: text, time: new Date() });
    this.setData({ messages, inputText: '' });

    websocket.send(text);
  },

  chooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        this.setData({
          showSubjectPicker: true,
          pendingImagePath: res.tempFiles[0].tempFilePath
        });
      }
    });
  },

  onSubjectSelect(e) {
    const subjectType = e.currentTarget.dataset.type;
    const filePath = this.data.pendingImagePath;
    const sessionId = this.data.sessionId;

    this.setData({ showSubjectPicker: false, pendingImagePath: null });

    const messages = this.data.messages;
    messages.push({ role: 'user', content: '[图片]', time: new Date(), isImage: true });
    this.setData({ messages });

    wx.showLoading({ title: '识别中...' });

    api.upload(filePath, subjectType, sessionId).then((res) => {
      wx.hideLoading();
      if (res.code === 200) {
        const data = res.data;
        messages.push({
          role: 'assistant',
          content: `📷 已识别题目：\n${data.cleanedText}\n\n---\n🤖 AI解答：\n${data.solution}`,
          time: new Date()
        });
        this.setData({ messages });
      } else {
        wx.showToast({ title: '识别失败', icon: 'error' });
      }
    }).catch((err) => {
      wx.hideLoading();
      wx.showToast({ title: '上传失败: ' + (err.errMsg || '网络错误'), icon: 'error' });
    });
  },

  onWebSocketMessage(msg) {
    const messages = this.data.messages;
    const last = messages[messages.length - 1];
    if (last && last.role === 'assistant' && last.streaming) {
      last.content += msg;
    } else {
      messages.push({ role: 'assistant', content: msg, time: new Date(), streaming: true });
    }
    this.setData({ messages });
  }
});
