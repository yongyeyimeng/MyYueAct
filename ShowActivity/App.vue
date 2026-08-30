<script>
	export default {
		onLaunch: function() {
			this.startBanPolling()
		},
		onShow: function() {
			this.startBanPolling()
		},
		onHide: function() {
			this.stopBanPolling()
		},
		methods: {
			startBanPolling() {
				this.refreshUserInfo()
				if (this.banTimer) return
				this.banTimer = setInterval(() => {
					this.refreshUserInfo()
				}, 10000)
			},
			stopBanPolling() {
				if (this.banTimer) {
					clearInterval(this.banTimer)
					this.banTimer = null
				}
			},
			refreshUserInfo() {
				const token = uni.getStorageSync('token')
				if (!token) return

				const API = require('./static/js/api.js').default
				uni.request({
					url: API.AUTH_ME,
					method: 'GET',
					header: {
						'Authorization': 'Bearer ' + token
					},
					success: (res) => {
						if (res.statusCode === 200 && res.data.code === 1) {
							uni.setStorageSync('userInfo', res.data.data)
							uni.removeStorageSync('banNotified')
						} else if (res.statusCode === 403 || (res.data && res.data.code === -1)) {
							const ban = res.data.data || {}
							const currentUser = uni.getStorageSync('userInfo') || {}
							currentUser.banned = true
							currentUser.role = currentUser.role || 'user'
							currentUser.banReason = ban.reason || ''
							currentUser.bannedUntil = ban.bannedUntil || ''
							uni.setStorageSync('userInfo', currentUser)
							if (!uni.getStorageSync('banNotified')) {
								uni.setStorageSync('banNotified', true)
								uni.showModal({
									title: '账号已被封禁',
									content: '封禁原因：' + (ban.reason || '未填写') + '\n封禁至：' + (ban.bannedUntil || '永久'),
									showCancel: false,
									confirmText: '我知道了'
								})
								uni.switchTab({
									url: '/pages/me/me'
								})
							}
						} else if (res.statusCode === 401) {
							uni.removeStorageSync('token')
							uni.removeStorageSync('userInfo')
						}
					}
				})
			}
		}
	}
</script>

<style>
	/* 公共样式 */
</style>
