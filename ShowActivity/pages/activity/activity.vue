<template>
<view>
		<view class="uni-title uni-common-pl">活动名称</view>
		<view class="uni-textarea">
			<textarea @input="onAtNameInput" :value="atName" auto-height placeholder="请输入活动名称" />
		</view>
		<view class="uni-title uni-common-pl">时间</view>
		<view class="uni-textarea">
			<textarea @input="onTimeInput" :value="time" auto-height placeholder="请输入活动时间" />
		</view>
		<view class="uni-title uni-common-pl">地点</view>
		<view class="uni-textarea">
			<textarea @input="onLocationInput" :value="location" auto-height placeholder="请输入活动地点" />
		</view>
		<view class="uni-title uni-common-pl">内容</view>
		<view class="uni-textarea">
			<textarea @input="onContentInput" :value="content" auto-height placeholder="请输入活动内容" />
		</view>
		<button class="uni-btn" @click="publishActivity">确认发布</button>
</view>
</template>
<script>
// 引入API配置
const API = require('../../static/js/api.js').default;

export default {
    data() {
        return {
            atName: '',
            time: '',
            location: '',
            content: '',
            promoter: null,
            num: 0
        }
    },
    onLoad() {
        // 加载用户信息
        this.loadUserInfo();
    },
    methods: {
        loadUserInfo() {
            const userInfo = uni.getStorageSync('userInfo');
            if (userInfo && userInfo.id) {
                this.promoter = userInfo.id;
                console.log('当前用户ID:', userInfo.id);
            } else {
                // 未登录直接回到“我的”
                uni.switchTab({
                    url: '/pages/me/me'
                });
            }
        },
        onAtNameInput: function (e) {
            this.atName = e.detail.value;
        },
        onTimeInput: function (e) {
            this.time = e.detail.value;
        },
        onLocationInput: function (e) {
            this.location = e.detail.value;
        },
        onContentInput: function (e) {
            this.content = e.detail.value;
        },
        publishActivity: function() {
            // 检查所有字段是否都已填写
            if (!this.atName || !this.time || !this.location || !this.content) {
                uni.showToast({
                    title: '请填写所有字段',
                    icon: 'none'
                });
                return;
            }
            
            // 检查用户是否已登录
            if (!this.promoter) {
                uni.switchTab({
                    url: '/pages/me/me'
                });
                return;
            }
            
            // 构造要发送的数据，根据后端定义调整类型
            const activityData = {
                atName: this.atName,
                time: this.time,
                location: this.location,
                content: this.content,
                promoter: this.promoter, // 保持为数字类型
                num: this.num // 保持为数字类型
            };
            
            console.log('准备发送的活动数据:', activityData);
            console.log('正在发送请求到:', API.ACTIVITIES_ADD);
            
            // 发送到后端接口
            uni.request({
                url: API.ACTIVITIES_ADD,
                method: 'POST',
                header: {
                    'Content-Type': 'application/json'
                },
                data: activityData,
                success: (res) => {
                    console.log('活动发布响应:', res);
                    if (res.statusCode >= 200 && res.statusCode < 300) {
                        uni.showToast({
                            title: '发布成功',
                            icon: 'success'
                        });
                        
                        // 发布成功后跳转到首页
                        setTimeout(() => {
                            uni.switchTab({
                                url: '/pages/index/index'
                            });
                        }, 1500);
                    } else {
                        console.error('服务器返回错误:', res);
                        uni.showToast({
                            title: res.data.message || '发布失败:' + (res.data.error || res.statusCode),
                            icon: 'none',
                            duration: 3000
                        });
                    }
                },
                fail: (err) => {
                    console.error('活动发布网络错误:', err);
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
</style>
