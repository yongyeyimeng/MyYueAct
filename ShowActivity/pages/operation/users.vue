<template>
	<view class="container">
		<view class="search-box">
			<input
				class="search-input"
				placeholder="输入昵称或用户ID搜索"
				:value="searchKeyword"
				@confirm="searchUsers"
				@input="onSearchInput"
			/>
			<button class="search-btn" @click="searchUsers">搜索</button>
		</view>

		<scroll-view
			class="scroll-area"
			scroll-y="true"
			@scrolltolower="onScrollToLower"
			lower-threshold="100"
		>
			<view class="user-list">
				<view class="user-item" v-for="user in users" :key="user.id">
					<view class="user-header">
						<text class="user-name">{{ user.nickname }}</text>
						<text class="user-id">ID: {{ user.id }}</text>
					</view>
					<view class="user-row">
						<text class="user-label">手机号</text>
						<text class="user-value">{{ user.phone || '未填写' }}</text>
					</view>
					<view v-if="user.banned" class="ban-info">
						<text class="ban-badge">已封禁</text>
						<text class="ban-reason">原因：{{ user.banReason || '未填写' }}</text>
						<text class="ban-until">截止：{{ formatBanUntil(user.bannedUntil) }}</text>
					</view>

					<view class="user-actions">
						<button v-if="!user.banned" class="ban-btn" @click="openBan(user)">封禁</button>
						<button v-else class="unban-btn" @click="unbanUser(user)">解封</button>
					</view>
				</view>

				<view v-if="users.length === 0 && !loading" class="empty">
					<text>暂无用户</text>
				</view>
			</view>

			<view class="loading-status">
				<view v-if="loading" class="loading"><text>加载中...</text></view>
				<view v-else-if="noMoreData && users.length > 0" class="no-more"><text>没有更多数据了</text></view>
			</view>
		</scroll-view>

		<view class="popup" v-if="showBanPopup">
			<view class="popup-content">
				<view class="popup-header">
					<text class="popup-title">封禁用户</text>
					<text class="close-btn" @click="closeBanPopup">×</text>
				</view>
				<view class="form-group">
					<text class="form-label">用户</text>
					<text class="form-value">{{ banForm.nickname }}（ID: {{ banForm.userId }}）</text>
				</view>
				<view class="form-group">
					<text class="form-label">封禁时长</text>
					<view class="duration-row">
						<input
							class="duration-input"
							type="number"
							v-model.number="banForm.duration"
							:disabled="banForm.durationUnit === 'permanent'"
							placeholder="时长"
						/>
						<view class="unit-tabs">
							<text
								class="unit-tab"
								:class="{ active: banForm.durationUnit === 'hour' }"
								@click="setDurationUnit('hour')"
							>小时</text>
							<text
								class="unit-tab"
								:class="{ active: banForm.durationUnit === 'day' }"
								@click="setDurationUnit('day')"
							>天</text>
							<text
								class="unit-tab"
								:class="{ active: banForm.durationUnit === 'permanent' }"
								@click="setDurationUnit('permanent')"
							>永久</text>
						</view>
					</view>
				</view>
				<view class="form-group">
					<text class="form-label">封禁原因</text>
					<textarea
						class="reason-input"
						v-model="banForm.reason"
						placeholder="请输入封禁原因"
					/>
				</view>
				<view class="form-actions">
					<button class="confirm-btn" @click="confirmBan">确认封禁</button>
					<button class="cancel-btn" @click="closeBanPopup">取消</button>
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
				users: [],
				page: 1,
				pageSize: 10,
				loading: false,
				noMoreData: false,
				searchKeyword: '',
				showBanPopup: false,
				banForm: {
					userId: null,
					nickname: '',
					duration: 1,
					durationUnit: 'day',
					reason: ''
				}
			}
		},
		onLoad() {
			this.loadUsers()
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
			loadUsers() {
				if (this.loading || this.noMoreData) return
				this.loading = true

				uni.request({
					url: API.ADMIN_USERS_LIST,
					method: 'GET',
					header: this.authHeader(),
					data: {
						page: this.page,
						pageSize: this.pageSize,
						keyword: this.searchKeyword
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
								this.users = rows
							} else {
								this.users = this.users.concat(rows)
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
			searchUsers() {
				this.page = 1
				this.noMoreData = false
				this.users = []
				this.loadUsers()
			},
			onScrollToLower() {
				if (!this.loading && !this.noMoreData) {
					this.page++
					this.loadUsers()
				}
			},
			formatBanUntil(value) {
				if (!value) return '永久'
				return String(value).replace('T', ' ')
			},
			openBan(user) {
				this.banForm = {
					userId: user.id,
					nickname: user.nickname,
					duration: 1,
					durationUnit: 'day',
					reason: ''
				}
				this.showBanPopup = true
			},
			closeBanPopup() {
				this.showBanPopup = false
			},
			setDurationUnit(unit) {
				this.banForm.durationUnit = unit
				if (unit === 'permanent') {
					this.banForm.duration = 0
				} else if (!this.banForm.duration || this.banForm.duration <= 0) {
					this.banForm.duration = 1
				}
			},
			confirmBan() {
				if (!this.banForm.reason.trim()) {
					uni.showToast({
						title: '请填写封禁原因',
						icon: 'none'
					})
					return
				}
				if (this.banForm.durationUnit !== 'permanent' && (!this.banForm.duration || this.banForm.duration <= 0)) {
					uni.showToast({
						title: '请填写正确的封禁时长',
						icon: 'none'
					})
					return
				}

				uni.request({
					url: API.ADMIN_USERS_BAN,
					method: 'POST',
					header: this.authHeader(),
					data: {
						userId: this.banForm.userId,
						reason: this.banForm.reason,
						duration: this.banForm.duration,
						durationUnit: this.banForm.durationUnit
					},
					success: (res) => {
						if (res.statusCode === 401 || res.statusCode === 403) {
							this.handleAuthError()
							return
						}
						if (res.statusCode === 200 && res.data.code === 1) {
							uni.showToast({
								title: '封禁成功',
								icon: 'success'
							})
							this.closeBanPopup()
							this.searchUsers()
						} else {
							uni.showToast({
								title: res.data.msg || '封禁失败',
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
			unbanUser(user) {
				uni.showModal({
					title: '确认解封',
					content: '确定要解封用户“' + user.nickname + '”吗？',
					success: (res) => {
						if (!res.confirm) return
						uni.request({
							url: API.ADMIN_USERS_UNBAN,
							method: 'POST',
							header: this.authHeader(),
							data: {
								userId: user.id
							},
							success: (res) => {
								if (res.statusCode === 401 || res.statusCode === 403) {
									this.handleAuthError()
									return
								}
								if (res.statusCode === 200 && res.data.code === 1) {
									uni.showToast({
										title: '解封成功',
										icon: 'success'
									})
									this.searchUsers()
								} else {
									uni.showToast({
										title: res.data.msg || '解封失败',
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
		background-color: #13b981;
		color: #ffffff;
		border-radius: 8rpx;
		font-size: 28rpx;
	}

	.scroll-area {
		height: calc(100vh - 180rpx);
		margin-top: 20rpx;
	}

	.user-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.user-item {
		padding: 24rpx;
		background-color: #ffffff;
		border-radius: 12rpx;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	}

	.user-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 16rpx;
		padding-bottom: 16rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.user-name {
		font-size: 32rpx;
		font-weight: bold;
		color: #333333;
	}

	.user-id {
		font-size: 24rpx;
		color: #999999;
	}

	.user-row {
		display: flex;
		margin-top: 10rpx;
		font-size: 26rpx;
	}

	.user-label {
		width: 120rpx;
		color: #888888;
	}

	.user-value {
		flex: 1;
		color: #444444;
	}

	.ban-info {
		margin-top: 16rpx;
		padding: 16rpx;
		background-color: #fff2f0;
		border-radius: 8rpx;
	}

	.ban-badge {
		display: inline-block;
		padding: 2rpx 12rpx;
		border-radius: 6rpx;
		background-color: #e5484d;
		color: #ffffff;
		font-size: 22rpx;
	}

	.ban-reason,
	.ban-until {
		display: block;
		margin-top: 8rpx;
		font-size: 24rpx;
		color: #c0392b;
	}

	.user-actions {
		display: flex;
		justify-content: flex-end;
		margin-top: 20rpx;
		padding-top: 20rpx;
		border-top: 1rpx solid #f0f0f0;
	}

	.ban-btn,
	.unban-btn {
		width: auto;
		margin: 0;
		padding: 0 30rpx;
		line-height: 56rpx;
		font-size: 26rpx;
		border-radius: 8rpx;
		color: #ffffff;
	}

	.ban-btn {
		background-color: #e5484d;
	}

	.unban-btn {
		background-color: #13b981;
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
		margin-bottom: 22rpx;
	}

	.form-label {
		display: block;
		margin-bottom: 10rpx;
		font-size: 26rpx;
		color: #666666;
	}

	.form-value {
		font-size: 28rpx;
		color: #333333;
	}

	.duration-row {
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.duration-input {
		width: 160rpx;
		padding: 12rpx 16rpx;
		border: 1rpx solid #e0e0e0;
		border-radius: 8rpx;
		font-size: 28rpx;
	}

	.unit-tabs {
		flex: 1;
		display: flex;
		gap: 10rpx;
	}

	.unit-tab {
		padding: 10rpx 20rpx;
		border-radius: 8rpx;
		background-color: #f0f0f0;
		color: #666666;
		font-size: 24rpx;
	}

	.unit-tab.active {
		background-color: #13b981;
		color: #ffffff;
	}

	.reason-input {
		width: 100%;
		height: 140rpx;
		padding: 16rpx;
		border: 1rpx solid #e0e0e0;
		border-radius: 8rpx;
		font-size: 28rpx;
		box-sizing: border-box;
	}

	.form-actions {
		display: flex;
		gap: 16rpx;
		margin-top: 30rpx;
	}

	.confirm-btn,
	.cancel-btn {
		flex: 1;
		margin: 0;
		border-radius: 8rpx;
		color: #ffffff;
	}

	.confirm-btn {
		background-color: #e5484d;
	}

	.cancel-btn {
		background-color: #cccccc;
	}
</style>
