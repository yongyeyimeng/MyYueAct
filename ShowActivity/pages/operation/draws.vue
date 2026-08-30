<template>
	<view class="container">
		<view class="search-box">
			<input
				class="search-input"
				type="number"
				v-model.number="userId"
				placeholder="输入用户ID查询抽签"
			/>
			<button class="search-btn" @click="searchDraws">查询</button>
		</view>

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
						<text class="info-line">抽签ID：{{ draw.id }}</text>
						<text class="info-line">创建者ID：{{ draw.creatorId }}</text>
						<text class="info-line">邀请码：{{ draw.inviteCode }}</text>
						<text class="info-line">参与：{{ draw.participantCount }} / {{ draw.peopleCount }}</text>
					</view>
				</view>

				<view v-if="draws.length === 0 && !loading && searched" class="empty">
					<text>该用户暂无抽签</text>
				</view>
			</view>

			<view class="loading-status">
				<view v-if="loading" class="loading"><text>加载中...</text></view>
				<view v-else-if="noMoreData && draws.length > 0" class="no-more"><text>没有更多了</text></view>
			</view>
		</scroll-view>

		<view class="popup" v-if="showDetail && selectedDraw">
			<view class="popup-content">
				<view class="popup-header">
					<text class="popup-title">{{ selectedDraw.content }}</text>
					<text class="close-btn" @click="closeDetail">×</text>
				</view>

				<view class="detail-info">
					<text class="info-line">抽签ID：{{ selectedDraw.id }}</text>
					<text class="info-line">创建者ID：{{ selectedDraw.creatorId }}</text>
					<text class="info-line">邀请码：{{ selectedDraw.inviteCode }}</text>
					<text class="info-line">参与：{{ selectedDraw.participantCount }} / {{ selectedDraw.peopleCount }}</text>
					<text class="info-line">状态：{{ statusText(selectedDraw.status) }}</text>
				</view>

				<view class="draw-items" v-if="selectedDraw.items && selectedDraw.items.length">
					<text class="section-title">抽签内容</text>
					<view class="draw-item" v-for="item in selectedDraw.items" :key="item.id">
						<text class="draw-item-name">{{ item.name }}</text>
						<text class="draw-item-count">{{ item.count }} 人</text>
					</view>
				</view>

				<view class="draw-result" v-if="selectedDraw.status === 'drawn'">
					<text class="section-title">抽签结果</text>
					<view class="result-item" v-for="item in selectedDraw.participants" :key="item.id">
						<text class="result-nickname">{{ item.nickname || ('用户' + item.userId) }}</text>
						<text class="result-name">{{ item.itemName || '未分配' }}</text>
					</view>
				</view>

				<button class="dissolve-btn" @click="dissolveDraw">解散抽签</button>
			</view>
		</view>
	</view>
</template>

<script>
	const API = require('../../static/js/api.js').default

	export default {
		data() {
			return {
				userId: null,
				draws: [],
				page: 1,
				pageSize: 10,
				loading: false,
				noMoreData: false,
				searched: false,
				showDetail: false,
				selectedDraw: null
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
			loadDraws() {
				if (!this.userId) {
					uni.showToast({
						title: '请输入用户ID',
						icon: 'none'
					})
					return
				}
				if (this.loading || this.noMoreData) return
				this.loading = true

				uni.request({
					url: API.ADMIN_DRAWS_LIST,
					method: 'GET',
					header: this.authHeader(),
					data: {
						userId: this.userId,
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
							this.searched = true
						} else {
							uni.showToast({
								title: res.data.msg || '查询失败',
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
			searchDraws() {
				this.page = 1
				this.noMoreData = false
				this.draws = []
				this.searched = false
				this.loadDraws()
			},
			onScrollToLower() {
				if (!this.loading && !this.noMoreData) {
					this.page++
					this.loadDraws()
				}
			},
			openDetail(draw) {
				uni.request({
					url: API.ADMIN_DRAWS_DETAIL,
					method: 'GET',
					header: this.authHeader(),
					data: {
						drawId: draw.id
					},
					success: (res) => {
						if (res.statusCode === 200 && res.data.code === 1) {
							this.selectedDraw = res.data.data
							this.showDetail = true
						} else {
							uni.showToast({
								title: res.data.msg || '加载详情失败',
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
			closeDetail() {
				this.showDetail = false
				this.selectedDraw = null
			},
			dissolveDraw() {
				uni.showModal({
					title: '确认解散',
					content: '解散后抽签及结果将被删除，且无法恢复',
					success: (res) => {
						if (!res.confirm) return
						uni.request({
							url: API.ADMIN_DRAWS_DISSOLVE,
							method: 'POST',
							header: this.authHeader(),
							data: {
								drawId: this.selectedDraw.id
							},
							success: (res) => {
								if (res.statusCode === 200 && res.data.code === 1) {
									uni.showToast({
										title: '已解散',
										icon: 'success'
									})
									this.closeDetail()
									this.searchDraws()
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

	.search-box {
		display: flex;
		padding: 10rpx;
		background-color: #ffffff;
		border-radius: 12rpx;
	}

	.search-input {
		flex: 1;
		padding: 10rpx 20rpx;
		border: 1rpx solid #e0e0e0;
		border-radius: 8rpx;
		font-size: 28rpx;
	}

	.search-btn {
		margin-left: 12rpx;
		background-color: #f59e0b;
		color: #ffffff;
		border-radius: 8rpx;
		font-size: 28rpx;
	}

	.scroll-area {
		height: calc(100vh - 180rpx);
		margin-top: 20rpx;
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

	.popup {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 999;
	}

	.popup-content {
		width: 92%;
		max-width: 700rpx;
		max-height: 85vh;
		overflow-y: auto;
		padding: 30rpx;
		background-color: #ffffff;
		border-radius: 12rpx;
	}

	.popup-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
		padding-bottom: 16rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.popup-title {
		flex: 1;
		font-size: 32rpx;
		font-weight: bold;
		color: #333333;
	}

	.close-btn {
		margin-left: 16rpx;
		font-size: 44rpx;
		color: #999999;
	}

	.detail-info {
		padding: 16rpx;
		background-color: #f9fafb;
		border-radius: 8rpx;
	}

	.draw-items,
	.draw-result {
		margin-top: 24rpx;
	}

	.section-title {
		display: block;
		margin-bottom: 12rpx;
		font-size: 28rpx;
		font-weight: bold;
		color: #333333;
	}

	.draw-item,
	.result-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 14rpx 18rpx;
		border-radius: 8rpx;
		margin-bottom: 10rpx;
	}

	.draw-item {
		background-color: #fff7e8;
	}

	.result-item {
		background-color: #f9fafb;
	}

	.draw-item-name,
	.result-nickname {
		font-size: 26rpx;
		color: #333333;
	}

	.draw-item-count,
	.result-name {
		font-size: 26rpx;
		font-weight: bold;
		color: #b45309;
	}

	.dissolve-btn {
		margin-top: 30rpx;
		background-color: #e5484d;
		color: #ffffff;
		border-radius: 8rpx;
	}
</style>
