<template>
	<view class="container" v-if="draw">
		<view class="detail-card">
			<view class="detail-header">
				<text class="detail-title">{{ draw.content }}</text>
				<text class="detail-status" :class="statusClass">{{ statusText }}</text>
			</view>

			<view class="detail-info">
				<text class="info-line">邀请码：{{ draw.inviteCode }}</text>
				<text class="info-line">参与：{{ draw.participantCount }} / {{ draw.peopleCount }}</text>
				<text class="info-line">可见性：{{ draw.visibility === 'public' ? '所有人可见' : '仅自己可见' }}</text>
				<text class="info-line">截止：{{ formatTime(draw.expiresAt) }}</text>
			</view>

			<view class="draw-items" v-if="draw.items && draw.items.length">
				<text class="section-title">抽签内容</text>
				<view class="draw-item" v-for="item in draw.items" :key="item.id">
					<text class="draw-item-name">{{ item.name }}</text>
					<text class="draw-item-count">{{ item.count }} 人</text>
				</view>
			</view>

			<view class="draw-result" v-if="draw.status === 'drawn'">
				<text class="section-title">抽签结果</text>
				<view class="result-item" v-for="item in draw.participants" :key="item.id">
					<text class="result-nickname">{{ item.nickname || ('用户' + item.userId) }}</text>
					<text class="result-name">{{ item.itemName || '未分配' }}</text>
				</view>
				<view v-if="draw.participants.length === 0" class="no-result">
					<text>暂无可见结果</text>
				</view>
			</view>
		</view>

		<button v-if="canDissolve" class="dissolve-btn" @click="dissolve">解散抽签</button>
	</view>
</template>

<script>
	const API = require('../../static/js/api.js').default

	export default {
		data() {
			return {
				drawId: null,
				draw: null,
				userInfo: null
			}
		},
		computed: {
			statusText() {
				if (!this.draw) return ''
				if (this.draw.status === 'drawn') return '已开签'
				if (this.draw.status === 'expired') return '已过期'
				return '抽签中'
			},
			statusClass() {
				if (!this.draw) return ''
				if (this.draw.status === 'drawn') return 'drawn'
				if (this.draw.status === 'expired') return 'expired'
				return 'open'
			},
			canDissolve() {
				if (!this.draw || !this.userInfo) return false
				return this.draw.creatorId === this.userInfo.id || this.userInfo.role === 'admin'
			}
		},
		onLoad(options) {
			this.userInfo = uni.getStorageSync('userInfo') || null
			this.drawId = options.drawId
			this.loadDetail()
		},
		methods: {
			authHeader() {
				const token = uni.getStorageSync('token')
				return {
					'Content-Type': 'application/json',
					'Authorization': 'Bearer ' + (token || '')
				}
			},
			loadDetail() {
				uni.request({
					url: API.DRAWS_DETAIL,
					method: 'GET',
					header: this.authHeader(),
					data: {
						drawId: this.drawId
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
							this.draw = res.data.data
						} else {
							uni.showToast({
								title: res.data.msg || '加载失败',
								icon: 'none'
							})
						}
					},
					fail: () => {
						uni.showToast({
							title: '网络请求失败',
							icon: 'none'
						})
					}
				})
			},
			formatTime(value) {
				if (!value) return ''
				return String(value).replace('T', ' ')
			},
			dissolve() {
				uni.showModal({
					title: '确认解散',
					content: '解散后抽签及结果将被删除，且无法恢复',
					success: (res) => {
						if (!res.confirm) return
						uni.request({
							url: API.DRAWS_DISSOLVE,
							method: 'POST',
							header: this.authHeader(),
							data: {
								drawId: this.draw.id
							},
							success: (res) => {
								if (res.statusCode === 200 && res.data.code === 1) {
									uni.showToast({
										title: '已解散',
										icon: 'success'
									})
									setTimeout(() => {
										uni.navigateBack()
									}, 800)
								} else {
									uni.showToast({
										title: res.data.msg || '解散失败',
										icon: 'none'
									})
								}
							},
							fail: () => {
								uni.showToast({
									title: '网络请求失败',
									icon: 'none'
								})
							}
						})
					}
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

	.detail-card {
		padding: 30rpx;
		background-color: #ffffff;
		border-radius: 12rpx;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	}

	.detail-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding-bottom: 20rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.detail-title {
		flex: 1;
		font-size: 36rpx;
		font-weight: bold;
		color: #222222;
	}

	.detail-status {
		margin-left: 16rpx;
		padding: 6rpx 16rpx;
		border-radius: 6rpx;
		font-size: 24rpx;
		color: #ffffff;
	}

	.detail-status.open {
		background-color: #f59e0b;
	}

	.detail-status.drawn {
		background-color: #13b981;
	}

	.detail-status.expired {
		background-color: #999999;
	}

	.detail-info {
		margin-top: 20rpx;
		padding: 20rpx;
		background-color: #f9fafb;
		border-radius: 8rpx;
	}

	.info-line {
		display: block;
		font-size: 26rpx;
		color: #555555;
		line-height: 1.8;
	}

	.draw-items,
	.draw-result {
		margin-top: 30rpx;
	}

	.section-title {
		display: block;
		margin-bottom: 16rpx;
		font-size: 28rpx;
		font-weight: bold;
		color: #333333;
	}

	.draw-item,
	.result-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 16rpx 20rpx;
		border-radius: 8rpx;
		margin-bottom: 12rpx;
	}

	.draw-item {
		background-color: #fff7e8;
	}

	.result-item {
		background-color: #f9fafb;
	}

	.draw-item-name,
	.result-nickname {
		font-size: 28rpx;
		color: #333333;
	}

	.draw-item-count,
	.result-name {
		font-size: 28rpx;
		font-weight: bold;
		color: #b45309;
	}

	.no-result {
		padding: 30rpx 0;
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
