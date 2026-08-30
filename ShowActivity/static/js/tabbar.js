function syncTabBarSelected() {
	const pages = getCurrentPages()
	if (!pages || pages.length === 0) return

	const currentPage = pages[pages.length - 1]
	if (!currentPage || typeof currentPage.getTabBar !== 'function') return

	const tabBar = currentPage.getTabBar()
	if (!tabBar || !tabBar.data || !tabBar.data.list) return

	const route = currentPage.route || ''
	let selected = 0
	for (let i = 0; i < tabBar.data.list.length; i++) {
		if (tabBar.data.list[i].pagePath === route) {
			selected = i
			break
		}
	}
	tabBar.setData({ selected })
}

module.exports = {
	syncTabBarSelected
}
