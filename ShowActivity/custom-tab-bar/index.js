Component({
  data: {
    selected: 0,
    list: [],
    color: '#7A7E83',
    selectedColor: '#3cc51f'
  },
  lifetimes: {
    attached() {
      this.loadTabList();
      this.listTimer = setInterval(() => {
        this.loadTabList();
      }, 5000);
    },
    detached() {
      if (this.listTimer) {
        clearInterval(this.listTimer);
        this.listTimer = null;
      }
    }
  },
  pageLifetimes: {
    show() {
      this.loadTabList();
      const pages = getCurrentPages();
      const current = pages[pages.length - 1];
      const route = current ? current.route : '';
      let index = -1;
      for (let i = 0; i < this.data.list.length; i++) {
        if (this.data.list[i].pagePath === route) {
          index = i;
          break;
        }
      }
      this.setData({ selected: index < 0 ? 0 : index });
    }
  },
  methods: {
    loadTabList() {
      const userInfo = wx.getStorageSync('userInfo');
      const isAdmin = userInfo && userInfo.role === 'admin';
      const isBanned = userInfo && userInfo.banned;
      let list;
      if (isBanned) {
        list = [
          { pagePath: 'pages/me/me', text: '我的' }
        ];
      } else {
        list = [
          { pagePath: 'pages/index/index', text: '首页' },
          { pagePath: 'pages/manage/manage', text: '管理' },
          { pagePath: 'pages/more/more', text: '更多' },
          { pagePath: 'pages/me/me', text: '我的' }
        ];
        if (isAdmin) {
          list.splice(1, 0, { pagePath: 'pages/operation/operation', text: '操作' });
        }
      }
      this.setData({ list });
    },
    switchTab(e) {
      const path = e.currentTarget.dataset.path;
      const index = e.currentTarget.dataset.index;
      this.setData({ selected: index });
      wx.switchTab({
        url: '/' + path
      });
    }
  }
});
