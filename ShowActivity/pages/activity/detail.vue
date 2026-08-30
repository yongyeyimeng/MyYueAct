<template>
	<view class="container">
		<view class="activity-detail" v-if="activity">
			<view class="activity-header">
				<text class="activity-title">{{ activity.atName }}</text>
				<text class="activity-time">{{ activity.time }}</text>
			</view>
			
			<view class="activity-info">
				<view class="info-item">
					<text class="label">地点：</text>
					<text class="value">{{ activity.location }}</text>
				</view>
				
				<view class="info-item">
					<text class="label">发起人：</text>
					<text class="value">{{ activity.nickname || activity.promoter }}</text>
				</view>
				
				<view class="info-item">
					<text class="label">参与人数：</text>
					<text class="value">{{ activity.num }}</text>
				</view>
				
				<view class="info-item">
					<text class="label">活动内容：</text>
					<text class="value content">{{ activity.content }}</text>
				</view>
			</view>
			
			<view class="action-section" v-if="userInfo && userInfo.id !== activity.promoter">
				<button 
					class="join-btn" 
					:disabled="joined" 
					:class="{ 'joined': joined }"
					@click="joinActivity">
					{{ joined ? '已参与' : '参与活动' }}
				</button>
			</view>
		</view>
		
		<view class="loading" v-else>
			<text>加载中...</text>
		</view>
	</view>
</template>

<script>
	// 引入API配置
	const API = require('../../static/js/api.js').default;
	
	export default {
		data() {
			return {
				activity: null,
				userInfo: null,
				joined: false
			}
		},
		
		onLoad(options) {
			this.loadUserInfo();
			// 从参数中直接获取活动信息
			if (options.activity) {
				try {
					this.activity = JSON.parse(decodeURIComponent(options.activity));
				} catch (e) {
					console.error('解析活动信息失败', e);
					uni.showToast({
						title: '数据解析失败',
						icon: 'none'
					});
				}
			} else {
				uni.showToast({
					title: '活动信息不存在',
					icon: 'none'
				});
				setTimeout(() => {
					uni.navigateBack();
				}, 1000);
			}
		},
		
		methods: {
			loadUserInfo() {
				const userInfo = uni.getStorageSync('userInfo');
				if (userInfo) {
					this.userInfo = userInfo;
				}
			},
			
			// 参与活动
			joinActivity() {
				if (!this.userInfo) {
					uni.switchTab({
						url: '/pages/me/me'
					});
					return;
				}
				
				uni.request({
					url: API.ACTIVITIES + '/join',
					method: 'POST',
					header: {
						'Content-Type': 'application/json'
					},
					data: {
						activityId: this.activity.id,
						userId: this.userInfo.id,
						paymentStatus: 'not_paid'
					},
					success: (res) => {
						if (res.statusCode === 200 && res.data.code === 1) {
							uni.showToast({
								title: '参与成功',
								icon: 'success'
							});
							this.joined = true;
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
	}
	
	.activity-detail {
		background-color: #ffffff;
		border-radius: 10rpx;
		padding: 30rpx;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
	}
	
	.activity-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 30rpx;
		padding-bottom: 20rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}
	
	.activity-title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
	}
	
	.activity-time {
		font-size: 24rpx;
		color: #999;
	}
	
	.activity-info {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}
	
	.info-item {
		display: flex;
	}
	
	.label {
		font-size: 28rpx;
		color: #666;
		width: 180rpx;
	}
	
	.value {
		font-size: 28rpx;
		color: #333;
		flex: 1;
	}
	
	.value.content {
		line-height: 1.6;
	}
	
	.action-section {
		margin-top: 40rpx;
	}
	
	.join-btn {
		width: 100%;
		background-color: #007AFF;
		color: white;
		border-radius: 10rpx;
		font-size: 32rpx;
		padding: 15rpx 0;
	}
	
	.join-btn[disabled] {
		background-color: #cccccc;
	}
	
	.joined {
		background-color: #cccccc !important;
	}
	
	.loading {
		text-align: center;
		padding: 50rpx 0;
		font-size: 28rpx;
		color: #999;
	}
</style>
