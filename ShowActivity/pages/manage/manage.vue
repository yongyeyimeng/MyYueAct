<template>
	<view class="container">
		<view class="content">
			<navigator url="/pages/activity/activity" open-type="navigate" hover-class="none">
				<button class="btn">添加活动</button>
			</navigator>
			
			<!-- 用户活动列表 -->
			<view class="activity-section" v-if="userInfo && userInfo.id">
				<text class="section-title">我发布的活动</text>
				<view class="activity-list">
					<view class="activity-item" v-for="activity in activities" :key="activity.id">
						<view class="activity-header">
							<text class="activity-title">{{ activity.atName }}</text>
						</view>
						<view class="activity-content">
							<text class="activity-time">时间：{{ activity.time }}</text>
							<text class="activity-location">地点：{{ activity.location }}</text>
							<text class="activity-description">内容：{{ activity.content }}</text>
							<view class="activity-footer">
								<text class="activity-participants">参与人数：{{ activity.num }}</text>
								<text class="activity-price" v-if="activity.aPrice !== null && activity.aPrice !== undefined">费用：¥{{ activity.aPrice }}</text>
							</view>
						</view>
						<view class="activity-actions">
							<button class="price-btn" @click="setPrice(activity)" v-if="activity.aPrice === null || activity.aPrice === undefined">设置费用</button>
							<button class="edit-btn" @click="editActivity(activity)">编辑</button>
							<button class="delete-btn" @click="deleteActivity(activity.id)">删除</button>
						</view>
					</view>
					
					<view v-if="activities.length === 0" class="no-activity">
						<text>暂无活动</text>
					</view>
				</view>
			</view>
			
			<!-- 编辑活动弹窗 -->
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
			
			<!-- 设置费用弹窗 -->
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
			
			<view v-if="!userInfo || !userInfo.id" class="login-tip">
				<text>请先登录</text>
			</view>
		</view>
	</view>
</template>

<script>
	// 引入API配置
	const API = require('../../static/js/api.js').default;
	
	export default {
		data() {
			return {
				userInfo: null,
				activities: [],
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
			if (this.redirectBannedToMe()) return;
			this.loadUserInfo();
		},
		onShow() {
			if (this.redirectBannedToMe()) return;
			require('../../static/js/tabbar.js').syncTabBarSelected()
			this.loadUserInfo();
			// 如果用户已登录，加载该用户发布的活动
			if (this.userInfo && this.userInfo.id) {
				this.loadUserActivities();
			}
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
					// 加载该用户发布的活动
					this.loadUserActivities();
				}
			},
			
			// 加载用户发布的活动
			loadUserActivities() {
				if (!this.userInfo || !this.userInfo.id) return;
				
				uni.request({
					url: API.ACTIVITIES_SHOW,
					method: 'GET',
					data: {
						promoterId: this.userInfo.id
					},
					success: (res) => {
						if (res.statusCode === 200 && res.data.code === 1) {
							this.activities = res.data.data.rows || res.data.data || [];
						} else {
							uni.showToast({
								title: '加载活动失败',
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
					}
				});
			},
			
			// 设置费用
			setPrice(activity) {
				this.priceForm.id = activity.id;
				this.priceForm.price = 0;
				this.showPricePopup = true;
			},
			
			// 保存费用
			savePrice() {
				if (this.priceForm.price < 0) {
					uni.showToast({
						title: '费用不能为负数',
						icon: 'none'
					});
					return;
				}
				
				uni.request({
					url: API.ACTIVITIES_SET_PRICE + '?id=' + this.priceForm.id + '&price=' + this.priceForm.price,
					method: 'PUT',
					success: (res) => {
						if (res.statusCode === 200 && res.data.code === 1) {
							uni.showToast({
								title: '费用设置成功',
								icon: 'success'
							});
							this.closePricePopup();
							// 重新加载活动列表
							this.loadUserActivities();
						} else {
							uni.showToast({
								title: res.data.msg || '费用设置失败',
								icon: 'none'
							});
						}
					},
					fail: (err) => {
						console.error('设置费用失败', err);
						uni.showToast({
							title: '网络请求失败',
							icon: 'none'
						});
					}
				});
			},
			
			// 关闭费用弹窗
			closePricePopup() {
				this.showPricePopup = false;
			},
			
			// 编辑活动
			editActivity(activity) {
				this.editForm = {
					id: activity.id,
					atName: activity.atName,
					time: activity.time,
					location: activity.location,
					content: activity.content,
					num: activity.num
				};
				this.showEditPopup = true;
			},
			
			// 删除活动
			deleteActivity(activityId) {
				uni.showModal({
					title: '确认删除',
					content: '确定要删除这个活动吗？此操作不可恢复',
					success: (res) => {
						if (res.confirm) {
							uni.request({
								url: API.ACTIVITIES_DELETE + '?id=' + activityId,
								method: 'DELETE',
								success: (res) => {
									if (res.statusCode === 200 && res.data.code === 1) {
										uni.showToast({
											title: '删除成功',
											icon: 'success'
										});
										// 重新加载活动列表
										this.loadUserActivities();
									} else {
										uni.showToast({
											title: res.data.msg || '删除失败',
											icon: 'none'
										});
									}
								},
								fail: (err) => {
									console.error('删除活动失败', err);
									uni.showToast({
										title: '网络请求失败',
										icon: 'none'
									});
								}
							});
						}
					}
				});
			},
			
			// 关闭编辑弹窗
			closeEditPopup() {
				this.showEditPopup = false;
			},
			
			// 保存活动
			saveActivity() {
				if (!this.editForm.atName) {
					uni.showToast({
						title: '请输入活动名称',
						icon: 'none'
					});
					return;
				}
				
				if (!this.editForm.time) {
					uni.showToast({
						title: '请输入活动时间',
						icon: 'none'
					});
					return;
				}
				
				if (!this.editForm.location) {
					uni.showToast({
						title: '请输入活动地点',
						icon: 'none'
					});
					return;
				}
				
				if (!this.editForm.content) {
					uni.showToast({
						title: '请输入活动内容',
						icon: 'none'
					});
					return;
				}
				
				uni.request({
					url: API.ACTIVITIES_UPDATE,
					method: 'PUT',
					header: {
						'Content-Type': 'application/json'
					},
					data: this.editForm,
					success: (res) => {
						if (res.statusCode === 200 && res.data.code === 1) {
							uni.showToast({
								title: '更新成功',
								icon: 'success'
							});
							this.closeEditPopup();
							// 重新加载活动列表
							this.loadUserActivities();
						} else {
							uni.showToast({
								title: res.data.msg || '更新失败',
								icon: 'none'
							});
						}
					},
					fail: (err) => {
						console.error('更新活动失败', err);
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
	
	.title {
		font-size: 36rpx;
		font-weight: bold;
		text-align: center;
		display: block;
		margin: 20rpx 0;
		color: #333;
	}
	
	.content {
		margin-top: 40rpx;
	}
	
	.btn {
		width: 100%;
		margin: 20rpx 0;
		background-color: #007AFF;
		color: white;
		border-radius: 10rpx;
	}
	
	.section-title {
		font-size: 32rpx;
		font-weight: bold;
		margin: 40rpx 0 20rpx 0;
		display: block;
		color: #333;
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
	
	.activity-content {
		display: flex;
		flex-direction: column;
		gap: 10rpx;
	}
	
	.activity-time,
	.activity-location,
	.activity-description {
		font-size: 28rpx;
		color: #666;
		line-height: 1.5;
	}
	
	.activity-footer {
		display: flex;
		justify-content: space-between;
		margin-top: 15rpx;
		padding-top: 15rpx;
		border-top: 1rpx solid #f0f0f0;
	}
	
	.activity-participants,
	.activity-price {
		font-size: 24rpx;
		color: #999;
	}
	
	.activity-actions {
		margin-top: 20rpx;
		padding-top: 20rpx;
		border-top: 1rpx solid #f0f0f0;
		text-align: right;
	}
	
	.edit-btn {
		background-color: #007AFF;
		color: white;
		border-radius: 6rpx;
		padding: 10rpx 20rpx;
		font-size: 24rpx;
		margin-right: 10rpx;
	}
	
	.delete-btn {
		background-color: #ff4d4f;
		color: white;
		border-radius: 6rpx;
		padding: 10rpx 20rpx;
		font-size: 24rpx;
	}
	
	.price-btn {
		background-color: #13CE66;
		color: white;
		border-radius: 6rpx;
		padding: 10rpx 20rpx;
		font-size: 24rpx;
		margin-right: 10rpx;
	}
	
	.no-activity {
		text-align: center;
		padding: 40rpx 0;
		color: #999;
	}
	
	.login-tip {
		text-align: center;
		padding: 40rpx 0;
		color: #999;
	}
	
	/* 弹窗样式 */
	.popup {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, 0.5);
		display: flex;
		justify-content: center;
		align-items: center;
		z-index: 999;
	}
	
	.popup-content {
		background-color: white;
		border-radius: 10rpx;
		width: 90%;
		max-width: 600rpx;
		padding: 30rpx;
	}
	
	.popup-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 30rpx;
	}
	
	.popup-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
	}
	
	.close-btn {
		font-size: 40rpx;
		color: #999;
	}
	
	.form-group {
		margin-bottom: 20rpx;
	}
	
	.form-input,
	.form-textarea {
		width: 100%;
		padding: 15rpx;
		border: 1rpx solid #ddd;
		border-radius: 6rpx;
		font-size: 28rpx;
		box-sizing: border-box;
	}
	
	.form-textarea {
		height: 150rpx;
		resize: none;
	}
	
	.form-actions {
		display: flex;
		justify-content: space-between;
		margin-top: 30rpx;
	}
	
	.save-btn,
	.cancel-btn {
		flex: 1;
		margin: 0 10rpx;
		border-radius: 6rpx;
		padding: 15rpx 0;
		font-size: 28rpx;
	}
	
	.save-btn {
		background-color: #007AFF;
		color: white;
	}
	
	.cancel-btn {
		background-color: #f5f5f5;
		color: #333;
		border: 1rpx solid #ddd;
	}
</style>
