### 设置信息

这些说明将帮助您设置开发环境，获取 muaCloud Android 应用的源代码并自行构建。如果您想帮助开发应用，请查看[贡献指南][0]。

第1和第2部分适用于任何环境。其余部分描述了如何在不同的工具环境中设置项目。目前我们推荐使用Android Studio（第2部分），但您也可以从命令行构建应用（第3部分）。

如果遇到任何问题，请删除 'android' 文件夹，从第1步重新开始。如果仍有问题，请打开一个新问题，描述您所做的、发生的事情和预期的结果。

### 0. 通用软件依赖

无论您选择何种IDE或构建工具，都需要一些工具。

[git][1] 用于访问muaCloud的不同版本的源代码。下载并安装适用于您操作系统的版本，并将git安装路径中的'bin/'目录添加到环境的PATH变量中，以便从任何位置使用。

[Android SDK][3] 必须安装以构建应用。根据所选IDE，安装方式有所不同。详细信息请参阅Google的[安装文档][4]。安装后，将Android SDK安装路径中的'tools/'和'platform-tools/'目录添加到环境的PATH变量中。

打开终端，输入 'android' 启动Android SDK Manager。要构建muaCloud Android应用，至少需要安装以下SDK包：

* Android SDK工具和Android SDK平台工具（已安装）；通常升级到最新版本是个好主意。
* 无需指定构建工具版本，Gradle插件会使用默认的最低版本。
* Android 12.0（API 31）SDK平台；需要构建muaCloud应用。

安装任何您认为有趣的软件包，例如模拟器。

有关其他软件依赖，请检查与您首选的IDE或构建系统相应的部分中的详细信息。

### 1. Fork并下载muaCloud/android仓库

您将需要 [git][1] 以访问muaCloud的不同版本的源代码。源代码托管在GitHub上，任何人都可以无需GitHub账户阅读代码。如果您想用自己的代码贡献应用开发，则需要一个GitHub账户。

### 2. 使用Android Studio

[Android Studio][5] 是目前官方的Android IDE。因此，我们推荐将其作为开发环境中使用的IDE。

我们建议使用Android Studio更新的稳定渠道中的最新版本。查看Android Studio的更新渠道，在菜单路径 'Help'/'Check for Update...'/对话框中的链接 'Updates'。

按照以下步骤在Android Studio中设置项目：

* 打开Android Studio并选择 'Import Project (Eclipse ADT, Gradle, etc)'。浏览文件系统到项目所在的 'android' 文件夹。Android Studio将创建所需的'.iml'文件。如果关闭项目但文件仍然存在，只需选择 'Open Project...'。文件选择器将显示一个Android头像作为文件夹图标，您可以选择重新打开项目。
* 导入项目后，Android Studio将尝试直接构建项目。要手动构建，请按照菜单路径 'Build'/'Make Project'，或点击工具栏中的 'Play' 按钮以在移动设备或模拟器中构建和运行项目。生成的APK文件将保存在项目文件夹中的 'build/outputs/apk/' 子目录中。

### 3. 在终端中使用Gradle

[Gradle][6] 是Android Studio使用的构建系统来管理Android应用的构建操作。您无需在系统中安装Gradle，Google推荐使用项目中包含的[Gradle wrapper][7]。

* 打开终端并进入包含仓库的 'android' 目录。
* 使用提供的Gradle wrapper运行 'clean' 和 'build' 任务：
    - Windows: ```gradlew.bat clean build```
    - Mac OS/Linux: ```./gradlew clean build```

第一次调用Gradle wrapper时，将自动下载正确的Gradle版本。需要互联网连接才能正常工作。

生成的APK文件保存在android/build/outputs/apk目录中，名为android-debug.apk。
