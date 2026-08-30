<template>
	<view class="container">
		<scroll-view
			class="scroll-area"
			scroll-y="true"
			@scrolltolower="onScrollToLower"
			lower-threshold="80"
		>
			<view class="draw-list">
				<view
					class="draw-item"
					v-for="draw in draws"
					:key="draw.id"
					@click="openDetail(draw)"
				>
					<view class="draw-header">
						<text class="draw-title">{{ draw.content }}</text>
						<text class="draw-status" :class="statusClass(draw.status)">{{ statusText(draw.status) }}</text>
					</view>
					<view class="draw-info">
						<text class="info-line">邀请码：{{ draw.inviteCode }}</text>
						<text class="info-line">参与：{{ draw.participantCount }} / {{ draw.peopleCount }}</text>
						<text class="info-line">截止：{{ formatTime(draw.expiresAt) }}</text>
					</view>
				</view>

				<view v-if="draws.length === 0 && !loading" class="empty">
					<text>暂无抽签</text>
				</view>
			</view>

			<view class="loading-status">
				<view v-if="loading" class="loading"><text>加载中...</text></view>
				<view v-else-if="noMoreData && draws.length > 0" class="no-more"><text>没有更多了</text></view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	const API = require('../../static/js/api.js').default

	export default {
		data() {
			return {
				draws: [],
				page: 1,
				pageSize: 20,
				loading: false,
				noMoreData: false
			}
		},
		onLoad() {
			if (!this.requireLogin()) return
			this.loadDraws()
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
			loadDraws() {
				if (this.loading || this.noMoreData) return
				this.loading = true

				uni.request({
					url: API.DRAWS_LIST,
					method: 'GET',
					header: this.authHeader(),
					data: {
						page: this.page,
						pageSize: this.pageSize
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
							const rows = res.data.data.rows || []
							this.noMoreData = rows.length < this.pageSize
							if (this.page === 1) {
								this.draws = rows
							} else {
								this.draws = this.draws.concat(rows)
							}
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
					},
					complete: () => {
						this.loading = false
					}
				})
			},
			onScrollToLower() {
				if (!this.loading && !this.noMoreData) {
					this.page++
					this.loadDraws()
				}
			},
			openDetail(draw) {
				uni.navigateTo({
					url: '/pages/draw/detail?drawId=' + draw.id
				})
			},
			statusText(status) {
				if (status === 'drawn') return '已开签'
				if (status === 'expired') return '已过期'
				return '抽签中'
			},
			statusClass(status) {
				if (status === 'drawn') return 'drawn'
				if (status === 'expired') return 'expired'
				return 'open'
			},
			formatTime(value) {
				if (!value) return ''
				return String(value).replace('T', ' ')
			}
		}
	}
</script>

<style>
	.container {
		min-height: 100vh;
		padding: 20rpx;
		background-color: #f5f6f8;
		box-sizing: border-box;
	}

	.scroll-area {
		height: calc(100vh - 40rpx);
	}

	.draw-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.draw-item {
		padding: 24rpx;
		background-color: #ffffff;
		border-radius: 12rpx;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	}

	.draw-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 14rpx;
		padding-bottom: 14rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.draw-title {
		flex: 1;
		font-size: 32rpx;
		font-weight: bold;
		color: #333333;
	}

	.draw-status {
		margin-left: 16rpx;
		padding: 4rpx 14rpx;
		border-radius: 6rpx;
		font-size: 22rpx;
		color: #ffffff;
	}

	.draw-status.open {
		background-color: #f59e0b;
	}

	.draw-status.drawn {
		background-color: #13b981;
	}

	.draw-status.expired {
		background-color: #999999;
	}

	.draw-info {
		padding: 14rpx;
		background-color: #f9fafb;
		border-radius: 8rpx;
	}

	.info-line {
		display: block;
		font-size: 26rpx;
		color: #555555;
		line-height: 1.8;
	}

	.empty,
	.loading,
	.no-more {
		text-align: center;
		padding: 40rpx 0;
		font-size: 26rpx;
		color: #999999;
	}
</style>
