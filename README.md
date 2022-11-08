# blankSpace
##实现白板功能
- 画笔颜色自定义
- 橡皮擦功能
- 缩放
- 移动
- 撤销画笔上一步的操作
- 清空画笔操作
- 旋转可自定义角度
- 可禁用白板功能
- 补充说明 demo中通过 WebSocket 实现互动功能 (WebSocket 请自行部署)
# 功能展示
![tu](https://user-images.githubusercontent.com/12062777/200496004-f4c855ae-e71b-4d9e-ba63-295e8fe5effc.png)

# 集成步骤
## 如果你的项目 Gradle 配置是在 7.0 以下，需要在 build.gradle 文件中加入
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
## 如果你的 Gradle 配置是 7.0 及以上，则需要在 settings.gradle 文件中加入
```
dependencyResolutionManagement {
    repositories {
        // JitPack 远程仓库：https://jitpack.io
        maven { url 'https://jitpack.io' }
    }
}
```
# 配置完远程仓库后，在项目 app 模块下的 build.gradle 文件中加入远程依赖

```
	dependencies {
	        implementation 'com.github.stone-gao:blankSpace:1.0.0'
	}
```
