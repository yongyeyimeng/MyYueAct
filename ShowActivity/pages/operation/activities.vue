<template>
	<view class="container">
		<view class="search-box">
			<input
				class="search-input"
				placeholder="输入活动名称搜索"
				:value="searchKeyword"
				@confirm="searchActivities"
				@input="onSearchInput"
			/>
			<button class="search-btn" @click="searchActivities">搜索</button>
		</view>

		<scroll-view
			class="scroll-area"
			scroll-y="true"
			@scrolltolower="onScrollToLower"
			lower-threshold="100"
		>
			<view class="activity-list">
				<view class="activity-item" v-for="activity in activities" :key="activity.id">
					<view class="activity-header">
						<text class="activity-title">{{ activity.atName }}</text>
						<text class="activity-time">{{ activity.time }}</text>
					</view>
					<view class="activity-row">
						<text class="activity-label">地点</text>
						<text class="activity-value">{{ activity.location }}</text>
					</view>
					<view class="activity-row">
						<text class="activity-label">发起人</text>
						<text class="activity-value">{{ activity.nickname || activity.promoter }}</text>
					</view>
					<view class="activity-row">
						<text class="activity-label">人数</text>
						<text class="activity-value">{{ activity.num }}</text>
					</view>
					<view class="activity-row">
						<text class="activity-label">费用</text>
						<text class="activity-value">{{ activity.aPrice === null || activity.aPrice === undefined ? '未设置' : activity.aPrice }}</text>
					</view>

					<view class="activity-actions">
						<button class="edit-btn" @click="editActivity(activity)">编辑</button>
						<button class="price-btn" @click="openPrice(activity)">设置费用</button>
						<button class="delete-btn" @click="deleteActivity(activity)">删除</button>
					</view>
				</view>

				<view v-if="activities.length === 0 && !loading" class="empty">
					<text>暂无活动</text>
				</view>
			</view>

			<view class="loading-status">
				<view v-if="loading" class="loading"><text>加载中...</text></view>
				<view v-else-if="noMoreData && activities.length > 0" class="no-more"><text>没有更多数据了</text></view>
			</view>
		</scroll-view>

		<view class="popup" v-if="showEditPopup">
			<view class="popup-content">
				<view class="popup-header">
					<text class="popup-title">编辑活动</text>
					<text class="close-btn" @click="closeEditPopup">×</text>
				</view>
				<view class="form-group">
					<input class="form-input" v-model="editForm.atName" placeholder="活动名称" />
				</view>
				<view class="form-group">
					<input class="form-input" v-model="editForm.time" placeholder="活动时间" />
				</view>
				<view class="form-group">
					<input class="form-input" v-model="editForm.location" placeholder="活动地点" />
				</view>
				<view class="form-group">
					<textarea class="form-textarea" v-model="editForm.content" placeholder="活动内容" />
				</view>
				<view class="form-group">
					<input class="form-input" type="number" v-model="editForm.num" placeholder="参与人数" />
				</view>
				<view class="form-actions">
					<button class="save-btn" @click="saveActivity">保存</button>
					<button class="cancel-btn" @click="closeEditPopup">取消</button>
				</view>
			</view>
		</view>

		<view class="popup" v-if="showPricePopup">
			<view class="popup-content">
				<view class="popup-header">
					<text class="popup-title">设置活动费用</text>
					<text class="close-btn" @click="closePricePopup">×</text>
				</view>
				<view class="form-group">
					<input class="form-input" type="number" v-model.number="priceForm.price" placeholder="请输入费用金额" />
				</view>
				<view class="form-actions">
					<button class="save-btn" @click="savePrice">保存</button>
					<button class="cancel-btn" @click="closePricePopup">取消</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	const API = require('../../static/js/api.js').default

	export default {
		data() {
			return {
				activities: [],
				page: 1,
				pageSize: 10,
				loading: false,
				noMoreData: false,
				searchKeyword: '',
				showEditPopup: false,
				showPricePopup: false,
				editForm: {
					id: null,
					atName: '',
					time: '',
					location: '',
					content: '',
					num: 0
				},
				priceForm: {
					id: null,
					price: 0
				}
			}
		},
		onLoad() {
			this.loadActivities()
		},
		methods: {
			authHeader() {
				const token = uni.getStorageSync('token')
				return {
					'Content-Type': 'application/json',
					'Authorization': 'Bearer ' + (token || '')
				}
			},
			onSearchInput(e) {
				this.searchKeyword = e.detail.value
			},
			loadActivities() {
				if (this.loading || this.noMoreData) return
				this.loading = true

				uni.request({
					url: API.ADMIN_ACTIVITIES_LIST,
					method: 'GET',
					header: this.authHeader(),
					data: {
						page: this.page,
						pageSize: this.pageSize,
						atName: this.searchKeyword
					},
					success: (res) => {
						if (res.statusCode === 401 || res.statusCode === 403) {
							this.handleAuthError()
							return
						}
						if (res.statusCode === 200 && res.data.code === 1) {
							const rows = res.data.data.rows || []
							this.noMoreData = rows.length < this.pageSize
							if (this.page === 1) {
								this.activities = rows
							} else {
								this.activities = this.activities.concat(rows)
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
			searchActivities() {
				this.page = 1
				this.noMoreData = false
				this.activities = []
				this.loadActivities()
			},
			onScrollToLower() {
				if (!this.loading && !this.noMoreData) {
					this.page++
					this.loadActivities()
				}
			},
			editActivity(activity) {
				this.editForm = {
					id: activity.id,
					atName: activity.atName,
					time: activity.time,
					location: activity.location,
					content: activity.content,
					num: activity.num
				}
				this.showEditPopup = true
			},
			closeEditPopup() {
				this.showEditPopup = false
			},
			saveActivity() {
				if (!this.editForm.atName || !this.editForm.time || !this.editForm.location) {
					uni.showToast({
						title: '请填写完整信息',
						icon: 'none'
					})
					return
				}
				uni.request({
					url: API.ADMIN_ACTIVITIES_UPDATE,
					method: 'PUT',
					header: this.authHeader(),
					data: this.editForm,
					success: (res) => {
						if (res.statusCode === 401 || res.statusCode === 403) {
							this.handleAuthError()
							return
						}
						if (res.statusCode === 200 && res.data.code === 1) {
							uni.showToast({
								title: '更新成功',
								icon: 'success'
							})
							this.closeEditPopup()
							this.searchActivities()
						} else {
							uni.showToast({
								title: res.data.msg || '更新失败',
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
			openPrice(activity) {
				this.priceForm.id = activity.id
				this.priceForm.price = activity.aPrice || 0
				this.showPricePopup = true
			},
			closePricePopup() {
				this.showPricePopup = false
			},
			savePrice() {
				if (this.priceForm.price < 0) {
					uni.showToast({
						title: '费用不能为负数',
						icon: 'none'
					})
					return
				}
				uni.request({
					url: API.ADMIN_ACTIVITIES_SET_PRICE + '?id=' + this.priceForm.id + '&price=' + this.priceForm.price,
					method: 'PUT',
					header: this.authHeader(),
					success: (res) => {
						if (res.statusCode === 401 || res.statusCode === 403) {
							this.handleAuthError()
							return
						}
						if (res.statusCode === 200 && res.data.code === 1) {
							uni.showToast({
								title: '费用设置成功',
								icon: 'success'
							})
							this.closePricePopup()
							this.searchActivities()
						} else {
							uni.showToast({
								title: res.data.msg || '设置失败',
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
			deleteActivity(activity) {
				uni.showModal({
					title: '确认删除',
					content: '确定要删除活动“' + activity.atName + '”吗？此操作不可恢复',
					success: (res) => {
						if (!res.confirm) return
						uni.request({
							url: API.ADMIN_ACTIVITIES_DELETE + '?id=' + activity.id,
							method: 'DELETE',
							header: this.authHeader(),
							success: (res) => {
								if (res.statusCode === 401 || res.statusCode === 403) {
									this.handleAuthError()
									return
								}
								if (res.statusCode === 200 && res.data.code === 1) {
									uni.showToast({
										title: '删除成功',
										icon: 'success'
									})
									this.searchActivities()
								} else {
									uni.showToast({
										title: res.data.msg || '删除失败',
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
			handleAuthError() {
				uni.showToast({
					title: '无权限或登录已过期',
					icon: 'none'
				})
			}
		}
	}
</script>

<style>
	.container {
		min-height: 100vh;
		padding: 20rpx;
		padding-bottom: 40rpx;
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
		background-color: #2f6fed;
		color: #ffffff;
		border-radius: 8rpx;
		font-size: 28rpx;
	}

	.scroll-area {
		height: calc(100vh - 180rpx);
		margin-top: 20rpx;
	}

	.activity-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.activity-item {
		padding: 24rpx;
		background-color: #ffffff;
		border-radius: 12rpx;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	}

	.activity-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 16rpx;
		padding-bottom: 16rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.activity-title {
		flex: 1;
		font-size: 32rpx;
		font-weight: bold;
		color: #333333;
	}

	.activity-time {
		margin-left: 16rpx;
		font-size: 24rpx;
		color: #999999;
	}

	.activity-row {
		display: flex;
		margin-top: 10rpx;
		font-size: 26rpx;
	}

	.activity-label {
		width: 120rpx;
		color: #888888;
	}

	.activity-value {
		flex: 1;
		color: #444444;
	}

	.activity-actions {
		display: flex;
		justify-content: flex-end;
		gap: 12rpx;
		margin-top: 20rpx;
		padding-top: 20rpx;
		border-top: 1rpx solid #f0f0f0;
	}

	.edit-btn,
	.price-btn,
	.delete-btn {
		width: auto;
		margin: 0;
		padding: 0 24rpx;
		line-height: 56rpx;
		font-size: 26rpx;
		border-radius: 8rpx;
		color: #ffffff;
	}

	.edit-btn {
		background-color: #2f6fed;
	}

	.price-btn {
		background-color: #13b981;
	}

	.delete-btn {
		background-color: #e5484d;
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
		width: 90%;
		max-width: 650rpx;
		padding: 30rpx;
		background-color: #ffffff;
		border-radius: 12rpx;
	}

	.popup-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 24rpx;
	}

	.popup-title {
		font-size: 34rpx;
		font-weight: bold;
		color: #333333;
	}

	.close-btn {
		font-size: 44rpx;
		color: #999999;
	}

	.form-group {
		margin-bottom: 20rpx;
	}

	.form-input,
	.form-textarea {
		width: 100%;
		padding: 16rpx;
		border: 1rpx solid #e0e0e0;
		border-radius: 8rpx;
		font-size: 28rpx;
		box-sizing: border-box;
	}

	.form-textarea {
		height: 160rpx;
	}

	.form-actions {
		display: flex;
		gap: 16rpx;
		margin-top: 30rpx;
	}

	.save-btn,
	.cancel-btn {
		flex: 1;
		margin: 0;
		border-radius: 8rpx;
		color: #ffffff;
	}

	.save-btn {
		background-color: #2f6fed;
	}

	.cancel-btn {
		background-color: #cccccc;
	}
</style>
