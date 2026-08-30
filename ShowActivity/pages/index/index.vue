<template>
	<view class="container">
		<!-- 搜索框 -->
		<view class="search-box">
			<input 
				class="search-input" 
				placeholder="请输入活动名称" 
				@confirm="searchActivities" 
				v-model="searchKeyword"
			/>
			<button class="search-btn" @click="searchActivities">搜索</button>
		</view>
		
		<!-- 活动列表 -->
		<scroll-view 
			class="scroll-area" 
			scroll-y="true" 
			@scrolltolower="onScrollToLower" 
			lower-threshold="100"
			enable-back-to-top="true"
		>
			<view class="activity-list">
				<view class="activity-item" v-for="activity in activities" :key="activity.id">
					<view class="activity-header">
						<text class="activity-title">{{ activity.atName }}</text>
						<text class="activity-time">{{ activity.time }}</text>
					</view>
					<view class="activity-content">
						<text class="activity-location">地点：{{ activity.location }}</text>
						<!-- 隐藏活动内容，需要点进去才显示 -->
						<navigator :url="'/pages/activity/detail?activity=' + encodeURIComponent(JSON.stringify(activity))" class="activity-detail-link">
							查看活动详情
						</navigator>
						<view class="activity-footer">
							<text class="activity-promoter">发起人：{{ activity.nickname || activity.promoter }}</text>
							<text class="activity-participants">参与人数：{{ activity.num }}</text>
						</view>
						<!-- 参与按钮 -->
						<view class="join-section" v-if="userInfo && userInfo.id !== activity.promoter">
							<button 
								class="join-btn" 
								:disabled="activity.joined" 
								:class="{ 'joined': activity.joined }"
								@click="joinActivity(activity)">
								{{ activity.joined ? '已参与' : '参与活动' }}
							</button>
						</view>
					</view>
				</view>
			</view>
			
			<view class="loading-status">
				<view v-if="loading" class="loading">
					<text>加载中...</text>
				</view>
				<view v-else-if="noMoreData" class="no-more-data">
					<text>没有更多数据了</text>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	// 引入API配置
	const API = require('../../static/js/api.js').default;
	
	export default {
		data() {
			return {
				activities: [],
				page: 1,
				pageSize: 5,
				loading: false,
				noMoreData: false,
				total: 0,
				searchKeyword: '', // 搜索关键词
				userInfo: null
			}
		},
		
		onLoad() {
			// 页面加载时获取第一页数据
			if (this.redirectBannedToMe()) return;
			this.loadUserInfo();
			this.loadActivities();
		},

		onShow() {
			if (this.redirectBannedToMe()) return;
			require('../../static/js/tabbar.js').syncTabBarSelected()
		},
		
		onPullDownRefresh() {
			// 下拉刷新
			this.refreshActivities();
		},
		
		methods: {
			redirectBannedToMe() {
				const userInfo = uni.getStorageSync('userInfo');
				if (userInfo && userInfo.banned) {
					uni.showToast({
						title: '账号已被封禁',
						icon: 'none'
					});
					uni.switchTab({
						url: '/pages/me/me'
					});
					return true;
				}
				return false;
			},
			loadUserInfo() {
				const userInfo = uni.getStorageSync('userInfo');
				if (userInfo) {
					this.userInfo = userInfo;
				}
			},
			
			// 加载活动列表
			loadActivities() {
				if (this.loading || this.noMoreData) return;
				
				this.loading = true;
				uni.request({
					url: API.ACTIVITIES_SHOW,
					method: 'GET',
					data: {
						page: this.page,
						pageSize: this.pageSize,
						atName: this.searchKeyword
					},
					success: (res) => {
						if (res.statusCode === 200) {
							const newActivities = res.data.data.rows || res.data.data || [];
							this.total = res.data.data.total || newActivities.length;
							
							// 处理活动数据，添加参与状态
							const processedActivities = newActivities.map(activity => {
								// 这里可以添加逻辑来检查用户是否已参与该活动
								// 现在暂时默认为未参与
								activity.joined = false;
								return activity;
							});
							
							if (this.page === 1) {
								this.activities = processedActivities;
							} else {
								this.activities = [...this.activities, ...processedActivities];
							}
							
							// 判断是否还有更多数据
							if (processedActivities.length < this.pageSize) {
								this.noMoreData = true;
							}
						} else {
							uni.showToast({
								title: '加载失败',
								icon: 'none'
							});
						}
					},
					fail: (err) => {
						console.error('加载活动列表失败', err);
						uni.showToast({
							title: '网络请求失败',
							icon: 'none'
						});
					},
					complete: () => {
						this.loading = false;
						uni.stopPullDownRefresh();
					}
				});
			},
			
			// 刷新活动列表
			refreshActivities() {
				this.page = 1;
				this.noMoreData = false;
				this.loadActivities();
			},
			
			// 滚动到底部时加载更多
			onScrollToLower() {
				if (!this.loading && !this.noMoreData) {
					this.page++;
					this.loadActivities();
				}
			},
			
			// 搜索活动
			searchActivities() {
				this.page = 1;
				this.noMoreData = false;
				this.activities = [];
				this.loadActivities();
			},
			
			// 参与活动
			joinActivity(activity) {
				if (!this.userInfo) {
					uni.switchTab({
						url: '/pages/me/me'
					});
					return;
				}
				
				uni.request({
					url: API.ACTIVITIES_JOIN,
					method: 'POST',
					header: {
						'Content-Type': 'application/json'
					},
					data: {
						activityId: activity.id,
						userId: this.userInfo.id,
						paymentStatus: 'not_paid'
					},
					success: (res) => {
						if (res.statusCode === 200 && res.data.code === 1) {
							uni.showToast({
								title: '参与成功',
								icon: 'success'
							});
							// 更新活动状态
							activity.joined = true;
						} else {
							uni.showToast({
								title: res.data.msg || '参与失败',
								icon: 'none'
							});
						}
					},
					fail: (err) => {
						console.error('参与活动失败', err);
						uni.showToast({
							title: '网络请求失败',
							icon: 'none'
						});
					}
				});
			}
		}
	}
</script>

<style>
	.container {
		padding: 20rpx;
		background-color: #f5f5f5;
		min-height: 100vh;
		padding-bottom: 140rpx;
		box-sizing: border-box;
	}
	
	.search-box {
		display: flex;
		margin-bottom: 20rpx;
		background-color: #ffffff;
		padding: 10rpx;
		border-radius: 10rpx;
	}
	
	.search-input {
		flex: 1;
		padding: 10rpx;
		border: 1rpx solid #e0e0e0;
		border-radius: 8rpx;
	}
	
	.search-btn {
		margin-left: 10rpx;
		background-color: #007AFF;
		color: white;
		border-radius: 8rpx;
	}
	
	.scroll-area {
		height: calc(100vh - 260rpx);
	}
	
	.activity-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}
	
	.activity-item {
		background-color: #ffffff;
		border-radius: 10rpx;
		padding: 20rpx;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
	}
	
	.activity-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 15rpx;
		padding-bottom: 15rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}
	
	.activity-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
	}
	
	.activity-time {
		font-size: 24rpx;
		color: #999;
	}
	
	.activity-content {
		display: flex;
		flex-direction: column;
		gap: 10rpx;
	}
	
	.activity-location,
	.activity-description {
		font-size: 28rpx;
		color: #666;
		line-height: 1.5;
	}
	
	.activity-detail-link {
		color: #007AFF;
		font-size: 28rpx;
		text-decoration: underline;
	}
	
	.activity-footer {
		display: flex;
		justify-content: space-between;
		margin-top: 15rpx;
		padding-top: 15rpx;
		border-top: 1rpx solid #f0f0f0;
	}
	
	.activity-promoter,
	.activity-participants {
		font-size: 24rpx;
		color: #999;
	}
	
	.loading-status {
		text-align: center;
		padding: 20rpx 0;
	}
	
	.loading text,
	.no-more-data text {
		font-size: 24rpx;
		color: #999;
	}
	
	.join-section {
		margin-top: 20rpx;
	}
	
	.join-btn {
		width: 100%;
		background-color: #007AFF;
		color: white;
		border-radius: 10rpx;
	}
	
	.join-btn[disabled] {
		background-color: #cccccc;
	}
	
	.joined {
		background-color: #cccccc !important;
	}
</style>
