<template>
	<view class="container">
		<view class="join-card" v-if="!drawDetail">
			<text class="join-title">参与抽签</text>
			<text class="join-desc">输入创建者分享的邀请码</text>
			<input
				class="code-input"
				type="number"
				maxlength="4"
				v-model="inviteCode"
				placeholder="4位邀请码"
			/>
			<button class="join-btn" @click="submit">参与抽签</button>
		</view>

		<view class="result-card" v-else>
			<view class="result-header">
				<text class="result-title">{{ drawDetail.content }}</text>
				<text class="result-status" :class="statusClass">{{ statusText }}</text>
			</view>

			<view class="result-info">
				<text class="info-line">参与人数：{{ drawDetail.participantCount }} / {{ drawDetail.peopleCount }}</text>
				<text class="info-line">可见性：{{ drawDetail.visibility === 'public' ? '所有人可见' : '仅自己可见' }}</text>
			</view>

			<view class="draw-items" v-if="drawDetail.items && drawDetail.items.length">
				<text class="result-section-title">抽签内容</text>
				<view class="draw-item" v-for="item in drawDetail.items" :key="item.id">
					<text class="draw-item-name">{{ item.name }}</text>
					<text class="draw-item-count">{{ item.count }} 人</text>
				</view>
			</view>

			<view v-if="hasResults" class="draw-result">
				<text class="result-section-title">抽签结果</text>
				<view class="result-item" v-for="item in drawDetail.participants" :key="item.id">
					<text class="result-nickname">{{ item.nickname || ('用户' + item.userId) }}</text>
					<text class="result-number">{{ item.itemName || ('第 ' + item.result + ' 号') }}</text>
				</view>
				<view v-if="drawDetail.participants.length === 0" class="no-result">
					<text>暂无可见结果</text>
				</view>
			</view>

			<view v-else class="waiting">
				<text>已参与，等待开签</text>
			</view>
		</view>

		<button v-if="canDissolve" class="dissolve-btn" @click="goDetail">解散抽签</button>
	</view>
</template>

<script>
	const API = require('../../static/js/api.js').default

	export default {
		data() {
			return {
				inviteCode: '',
				drawDetail: null,
				submitting: false,
				userInfo: null
			}
		},
		onLoad() {
			this.userInfo = uni.getStorageSync('userInfo') || null
		},
		computed: {
			hasResults() {
				if (!this.drawDetail || !this.drawDetail.participants) return false
				return this.drawDetail.participants.some(item => item.itemName)
			},
			statusText() {
				if (!this.drawDetail) return ''
				if (this.drawDetail.status === 'drawn') return '已开签'
				if (this.drawDetail.status === 'expired') return '已过期'
				return '抽签中'
			},
			statusClass() {
				if (!this.drawDetail) return ''
				if (this.drawDetail.status === 'drawn') return 'drawn'
				if (this.drawDetail.status === 'expired') return 'expired'
				return 'open'
			},
			canDissolve() {
				if (!this.drawDetail || !this.userInfo) return false
				return this.drawDetail.creatorId === this.userInfo.id || this.userInfo.role === 'admin'
			}
		},
		methods: {
			authHeader() {
				const token = uni.getStorageSync('token')
				return {
					'Content-Type': 'application/json',
					'Authorization': 'Bearer ' + (token || '')
				}
			},
			requireLogin() {
				const userInfo = uni.getStorageSync('userInfo')
				const token = uni.getStorageSync('token')
				if (!userInfo || !token) {
					uni.switchTab({
						url: '/pages/me/me'
					})
					return false
				}
				return true
			},
			submit() {
				if (!this.requireLogin()) return
				if (!this.inviteCode || this.inviteCode.length !== 4) {
					uni.showToast({
						title: '请输入4位邀请码',
						icon: 'none'
					})
					return
				}
				if (this.submitting) return
				this.submitting = true

				uni.request({
					url: API.DRAWS_JOIN,
					method: 'POST',
					header: this.authHeader(),
					data: {
						inviteCode: this.inviteCode
					},
					success: (res) => {
						if (res.statusCode === 401 || res.statusCode === 403) {
							uni.showToast({
								title: res.data.msg || '无权限或登录已过期',
								icon: 'none'
							})
							return
						}
						if (res.statusCode === 200 && res.data.code === 1) {
							this.drawDetail = res.data.data
							this.showOwnResult()
						} else {
							uni.showToast({
								title: res.data.msg || '参与失败',
								icon: 'none'
							})
						}
					},
					fail: () => {
						uni.showToast({
							title: '网络请求失败',
							icon: 'none'
						})
					},
					complete: () => {
						this.submitting = false
					}
				})
			},
			showOwnResult() {
				if (!this.drawDetail) return
				const participants = this.drawDetail.participants || []
				const own = participants.find(item => this.userInfo && item.userId === this.userInfo.id)
				if (own && own.itemName) {
					uni.showModal({
						title: '抽签结果',
						content: '你抽到了：' + own.itemName,
						showCancel: false,
						confirmText: '知道了'
					})
				}
			},
			goDetail() {
				uni.navigateTo({
					url: '/pages/draw/detail?drawId=' + this.drawDetail.id
				})
			}
		}
	}
</script>

<style>
	.container {
		min-height: 100vh;
		padding: 30rpx;
		background-color: #f5f6f8;
		box-sizing: border-box;
	}

	.join-card,
	.result-card {
		padding: 30rpx;
		background-color: #ffffff;
		border-radius: 12rpx;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	}

	.join-title {
		display: block;
		font-size: 38rpx;
		font-weight: bold;
		color: #222222;
	}

	.join-desc {
		display: block;
		margin-top: 10rpx;
		font-size: 26rpx;
		color: #888888;
	}

	.code-input {
		margin-top: 40rpx;
		padding: 24rpx;
		text-align: center;
		border: 1rpx solid #e0e0e0;
		border-radius: 8rpx;
		font-size: 48rpx;
		letter-spacing: 10rpx;
	}

	.join-btn {
		margin-top: 40rpx;
		background-color: #2f6fed;
		color: #ffffff;
		border-radius: 8rpx;
	}

	.result-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding-bottom: 20rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.result-title {
		flex: 1;
		font-size: 34rpx;
		font-weight: bold;
		color: #222222;
	}

	.result-status {
		margin-left: 16rpx;
		padding: 6rpx 16rpx;
		border-radius: 6rpx;
		font-size: 24rpx;
		color: #ffffff;
	}

	.result-status.open {
		background-color: #f59e0b;
	}

	.result-status.drawn {
		background-color: #13b981;
	}

	.result-status.expired {
		background-color: #999999;
	}

	.result-info {
		margin-top: 20rpx;
		padding: 20rpx;
		background-color: #f9fafb;
		border-radius: 8rpx;
	}

	.draw-items {
		margin-top: 24rpx;
	}

	.draw-item {
		display: flex;
		justify-content: space-between;
		padding: 14rpx 20rpx;
		background-color: #fff7e8;
		border-radius: 8rpx;
		margin-bottom: 10rpx;
	}

	.draw-item-name {
		font-size: 26rpx;
		color: #333333;
	}

	.draw-item-count {
		font-size: 26rpx;
		color: #b45309;
	}

	.info-line {
		display: block;
		font-size: 26rpx;
		color: #555555;
		line-height: 1.8;
	}

	.draw-result {
		margin-top: 30rpx;
	}

	.result-section-title {
		display: block;
		margin-bottom: 16rpx;
		font-size: 28rpx;
		font-weight: bold;
		color: #333333;
	}

	.result-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 18rpx 20rpx;
		background-color: #f9fafb;
		border-radius: 8rpx;
		margin-bottom: 12rpx;
	}

	.result-nickname {
		font-size: 28rpx;
		color: #333333;
	}

	.result-number {
		font-size: 28rpx;
		font-weight: bold;
		color: #b45309;
	}

	.no-result,
	.waiting {
		padding: 40rpx 0;
		text-align: center;
		font-size: 26rpx;
		color: #999999;
	}

	.dissolve-btn {
		margin-top: 30rpx;
		background-color: #e5484d;
		color: #ffffff;
		border-radius: 8rpx;
	}
</style>
