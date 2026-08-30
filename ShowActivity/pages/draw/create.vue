<template>
	<view class="container">
		<view class="form-card" v-if="!createdDraw">
			<view class="form-group">
				<text class="form-label">抽签内容（1-10个）</text>
				<view class="item-row" v-for="(item, index) in items" :key="index">
					<input
						class="item-name"
						v-model="item.name"
						placeholder="内容名称"
					/>
					<input
						class="item-count"
						type="number"
						v-model.number="item.count"
						placeholder="人数"
					/>
					<text
						class="remove-btn"
						v-if="items.length > 1"
						@click="removeItem(index)"
					>删除</text>
				</view>
				<button
					class="add-btn"
					:disabled="items.length >= 10"
					@click="addItem"
				>新建内容</button>
			</view>

			<view class="form-group">
				<text class="form-label">结果可见性</text>
				<view class="visibility-tabs">
					<text
						class="visibility-tab"
						:class="{ active: visibility === 'public' }"
						@click="setVisibility('public')"
					>所有人可见</text>
					<text
						class="visibility-tab"
						:class="{ active: visibility === 'private' }"
						@click="setVisibility('private')"
					>仅自己可见</text>
				</view>
			</view>

			<button class="submit-btn" @click="submit">发布抽签</button>
		</view>

		<view class="success-card" v-else>
			<text class="success-title">发布成功</text>
			<text class="success-desc">邀请码有效一小时，或该抽签人数已满时失效</text>
			<view class="invite-code">{{ createdDraw.inviteCode }}</view>
			<view class="success-info">
				<view class="item-summary" v-for="item in createdDraw.items" :key="item.id">
					<text class="item-summary-name">{{ item.name }}</text>
					<text class="item-summary-count">{{ item.count }} 人</text>
				</view>
				<text class="info-line">参与总人数：{{ createdDraw.peopleCount }}</text>
				<text class="info-line">截止时间：{{ formatTime(createdDraw.expiresAt) }}</text>
			</view>
			<button class="copy-btn" @click="copyCode">复制邀请码</button>
			<button class="back-btn" @click="goBack">返回抽签首页</button>
		</view>
	</view>
</template>

<script>
	const API = require('../../static/js/api.js').default

	export default {
		data() {
			return {
				items: [
					{ name: '', count: 1 },
					{ name: '', count: 1 }
				],
				visibility: 'public',
				createdDraw: null,
				submitting: false
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
			addItem() {
				if (this.items.length >= 10) {
					uni.showToast({
						title: '最多添加10个内容',
						icon: 'none'
					})
					return
				}
				this.items.push({ name: '', count: 1 })
			},
			removeItem(index) {
				if (this.items.length <= 1) return
				this.items.splice(index, 1)
			},
			setVisibility(value) {
				this.visibility = value
			},
			formatTime(value) {
				if (!value) return ''
				return String(value).replace('T', ' ')
			},
			submit() {
				if (!this.requireLogin()) return
				for (let i = 0; i < this.items.length; i++) {
					if (!this.items[i].name.trim()) {
						uni.showToast({
							title: '请填写第' + (i + 1) + '个内容名称',
							icon: 'none'
						})
						return
					}
					if (!this.items[i].count || this.items[i].count < 1) {
						uni.showToast({
							title: '第' + (i + 1) + '个内容人数至少为1',
							icon: 'none'
						})
						return
					}
				}
				if (this.submitting) return
				this.submitting = true

				uni.request({
					url: API.DRAWS_CREATE,
					method: 'POST',
					header: this.authHeader(),
					data: {
						content: '抽签',
						visibility: this.visibility,
						items: this.items
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
							this.createdDraw = res.data.data
						} else {
							uni.showToast({
								title: res.data.msg || '发布失败',
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
			copyCode() {
				uni.setClipboardData({
					data: this.createdDraw.inviteCode,
					success: () => {
						uni.showToast({
							title: '已复制',
							icon: 'success'
						})
					}
				})
			},
			goBack() {
				const pages = getCurrentPages()
				if (pages.length > 1) {
					uni.navigateBack()
				} else {
					uni.redirectTo({
						url: '/pages/draw/draw'
					})
				}
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

	.form-card,
	.success-card {
		padding: 30rpx;
		background-color: #ffffff;
		border-radius: 12rpx;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	}

	.form-group {
		margin-bottom: 26rpx;
	}

	.form-label {
		display: block;
		margin-bottom: 12rpx;
		font-size: 28rpx;
		color: #444444;
	}

	.item-row {
		display: flex;
		align-items: center;
		gap: 12rpx;
		margin-bottom: 14rpx;
	}

	.item-name {
		flex: 1;
		padding: 14rpx 16rpx;
		border: 1rpx solid #e0e0e0;
		border-radius: 8rpx;
		font-size: 28rpx;
	}

	.item-count {
		width: 130rpx;
		padding: 14rpx 16rpx;
		border: 1rpx solid #e0e0e0;
		border-radius: 8rpx;
		font-size: 28rpx;
	}

	.remove-btn {
		padding: 12rpx 16rpx;
		background-color: #fff2f0;
		color: #e5484d;
		border-radius: 8rpx;
		font-size: 24rpx;
	}

	.add-btn {
		margin-top: 6rpx;
		background-color: #fff7e8;
		color: #b45309;
		border: 1rpx dashed #f59e0b;
		border-radius: 8rpx;
		font-size: 26rpx;
	}

	.visibility-tabs {
		display: flex;
		gap: 16rpx;
	}

	.visibility-tab {
		flex: 1;
		padding: 18rpx 0;
		text-align: center;
		border-radius: 8rpx;
		background-color: #f0f0f0;
		color: #666666;
		font-size: 26rpx;
	}

	.visibility-tab.active {
		background-color: #f59e0b;
		color: #ffffff;
	}

	.submit-btn {
		margin-top: 20rpx;
		background-color: #f59e0b;
		color: #ffffff;
		border-radius: 8rpx;
	}

	.success-card {
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.success-title {
		font-size: 40rpx;
		font-weight: bold;
		color: #222222;
	}

	.success-desc {
		margin-top: 12rpx;
		font-size: 26rpx;
		color: #888888;
		text-align: center;
	}

	.invite-code {
		margin: 40rpx 0;
		padding: 30rpx 50rpx;
		background-color: #fff7e8;
		border: 2rpx dashed #f59e0b;
		border-radius: 12rpx;
		font-size: 72rpx;
		font-weight: bold;
		letter-spacing: 12rpx;
		color: #b45309;
	}

	.success-info {
		width: 100%;
		padding: 24rpx;
		background-color: #f9fafb;
		border-radius: 8rpx;
	}

	.item-summary {
		display: flex;
		justify-content: space-between;
		padding: 10rpx 0;
		border-bottom: 1rpx solid #eeeeee;
	}

	.item-summary-name {
		font-size: 26rpx;
		color: #333333;
	}

	.item-summary-count {
		font-size: 26rpx;
		color: #b45309;
	}

	.info-line {
		display: block;
		font-size: 26rpx;
		color: #555555;
		line-height: 1.8;
	}

	.copy-btn,
	.back-btn {
		width: 100%;
		margin-top: 20rpx;
		border-radius: 8rpx;
	}

	.copy-btn {
		background-color: #f59e0b;
		color: #ffffff;
	}

	.back-btn {
		background-color: #f0f0f0;
		color: #555555;
	}
</style>
