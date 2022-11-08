# blankSpace
白板
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
