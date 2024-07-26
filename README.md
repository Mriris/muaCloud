# [muaCloud](mua@dlmu.edu.cn) Android app

| <img src="docs_resources/spaces_device.png"> | <img src="docs_resources/detail_view_device.png"> |
| ---------------------------------------------- | -------------------------------------------  | 

## mua成员

**任务分工:** <br>

| 姓名  | 任务 | 
|-----|---------------------------|
| 燕庭轩 | 选题，项目计划，概要设计，检查任务进度，技术指导，代码总体设计，服务器部署 | 
| 谭佳熙 | 需求分析，详细设计，资源设计（动画，布局），文档撰写，代码审计，界面开发，前后端交互设计| 
| 符镇侃 | 后端开发，测试，代码文档撰写，数据流分析，API设计与开发，数据库设计  | 
| 赵网  | 数据流分析，规定数据流程，测试，数据库开发，高效缓存与内存管理，依赖注入技术，深度优化   | 

## muaCloud概述

### 项目背景
随着信息化时代的到来，数据存储和管理已成为企业和个人的重要需求。网盘作为一种方便、快捷的数据存储解决方案，得到了广泛地应用。目前，市场上已有的网盘产品如 Google Drive、Dropbox、OneDrive、百度网盘等，在为用户提供存储和共享服务的同时，也存在一些不可忽视的缺陷。

#### 数据隐私和安全性问题
国外网盘如 Google Drive、Dropbox 等虽然在功能和用户体验上做得较好，但由于服务器大多位于国外，存在数据隐私和安全性的问题。特别是在一些敏感数据的存储和传输过程中，用户数据可能面临被窃取或泄露的风险。国内网盘如百度网盘虽然服务器位于本土，数据隐私相对有保障，但也存在因内部管理不善或外部攻击而导致的数据泄露风险。

#### 存储容量和流量限制
无论是 Google Drive、Dropbox 还是百度网盘，虽然提供一定的免费存储空间，但容量有限，用户需要支付高昂的费用直接购买空间或充值 VIP 才能储存更多的文件。此外，这些网盘在传输速度和流量上也有限制，特别是百度网盘对非会员用户进行了限制，而且广告和推广信息较多，影响用户使用体验。

### 项目概述
针对上述问题，muaCloud 设计并实现了一个文件存储、同步、分享和管理的系统。该系统基于 PHP 提供安全、可靠且易于使用的移动网盘功能。用户可以在不同设备之间同步文件，进行文件共享，通过账号身份验证确保数据的安全性和隐私保护，并能随时随地访问自己的数据。

### 开发环境
- 服务器端：Linux本地服务器，PHP，MySQL
- 客户端：React Native（适用于 Android 平台）
- 开发工具：Android Studio，Powderdesigner，phpMyAdmin

### 系统分析

#### 需求描述

##### 功能需求
- **注册登录**：提供用户注册功能，使新用户可以创建账户。注册成功后，用户可以通过登录功能进行身份验证，进入系统。
- **文件管理**：用户可以方便地上传文件到系统中，并可以在需要时下载这些文件。此外，用户还可以删除不再需要的文件。
- **文件共享**：用户可以将文件共享给其他用户，并可以设置不同的访问权限（如只读、编辑、下载等），确保文件的安全和隐私。
- **版本控制**：提供文件的版本控制功能，用户可以查看文件的历史版本，恢复到之前的版本，以防止误操作导致的数据丢失。
- **日志记录**：记录用户的操作日志，包括文件上传、下载、删除和共享等操作，以便于审计和追踪用户行为，确保系统的安全和透明。
- **用户管理**：管理员可以管理系统中的用户，包括创建、删除和修改用户信息，分配用户角色和权限。
- **搜索功能**：用户可以通过关键字搜索系统中的文件和共享内容，快速找到所需资料。

##### 非功能需求
- **安全性**：系统必须确保用户数据的安全性和隐私保护。所有的文件传输和存储应采用加密技术，用户权限控制应严格，防止未授权访问。
- **可用性**：系统具有高可用性，确保用户可以随时访问。系统应支持负载均衡和容错机制，防止单点故障影响服务。
- **扩展性**：系统具备良好的扩展性，以支持未来的功能扩展和用户增长需求。系统架构设计应模块化，便于功能的增加和修改。
- **性能**：系统具备高效的性能，能够快速响应用户的操作请求。文件上传和下载速度应尽可能快，搜索和共享功能应及时响应。
- **兼容性**：系统兼容多种操作系统和设备，包括 Windows、Linux 以及移动设备，确保用户可以在不同平台上无缝使用。
- **用户体验**：系统具备良好的用户体验，界面设计应简洁友好，操作流程应便捷顺畅，帮助用户快速上手使用系统。

#### 需求详情

##### 文件操作
- **文件上传**：用户可以从本地设备上传文件到网盘，支持批量上传和大文件断点续传。
- **文件下载**：用户可以将网盘中的文件下载到本地设备，支持批量下载和下载速度限制设置。
- **新建文件夹**：用户可以新建文件夹来整理和分类文件。
- **文件移动和复制**：用户可以将文件在不同文件夹之间移动或复制。
- **文件重命名**：用户可以对文件和文件夹进行重命名。
- **文件删除和恢复**：用户可以删除文件，删除的文件将移动到回收站，用户可以从回收站恢复已删除的文件。

##### 查询展示
- **文件搜索**：用户可以通过文件名、文件类型、上传时间等条件搜索文件。
- **高级搜索**：用户可以根据文件内容进行全文搜索，提高文件检索效率。
- **文件预览**：支持常见文件格式（如图片、视频、PDF、文档等）的在线预览。
- **视图**：用户可以选择以列表或缩略图方式查看文件和文件夹。

##### 分享
- **生成分享链接**：用户可以生成文件分享链接，设置分享有效期和访问权限，支持分享到社交媒体。
- **权限设置**：用户可以设置分享链接的访问权限（如只读、可编辑）。

### 系统设计

#### 功能结构

##### 用户管理模块
- **用户注册**：用户通过填写用户名、密码、邮箱和手机号注册。系统验证用户输入的信息，确保格式正确并检查是否已存在。成功注册后，用户信息存储在数据库中。
- **用户登录**：用户通过用户名/邮箱/手机号和密码登录。系统验证用户输入的信息，确保身份合法性。登录成功后，生成用户会话信息。
- **用户信息管理**：用户可以查看和修改个人信息（如密码、邮箱、手机号等）。系统提供密码重置和邮箱验证功能。

##### 文件管理模块
- **文件上传**：用户选择文件上传到网盘。系统检查文件大小和类型，确保符合要求。文件上传完成后，存储在服务器并更新文件信息数据库。
- **文件下载**：用户选择文件从网盘下载到本地设备。系统检查用户下载权限，确保安全。文件下载完成后，用户可以在本地设备查看文件。
- **文件浏览**：用户在网盘中浏览文件和文件夹，支持列表和缩略图视图。系统提供按名称、类型、上传时间等条件排序和过滤文件的功能。
- **文件重命名**：用户选择文件或文件夹重命名。系统检查新名称的有效性，并更新数据库。
- **文件移动和复制**：用户选择文件或文件夹移动或复制到目标文件夹。系统执行操作并更新文件路径信息。
- **文件删除和恢复**：用户选择文件或文件夹删除，移动到回收站。用户可以从回收站恢复文件或彻底删除文件。

##### 文件共享模块
- **生成分享链接**：用户选择文件或文件夹生成分享链接。设置分享权限（只读、可编辑）和有效期。系统生成分享链接并更新共享信息数据库。
- **权限设置**：用户可以为分享链接设置不同的访问权限。系统在访问分享链接时，检查并控制用户权限。

##### 版本控制模块
- **查看文件历史版本**：用户选择文件查看历史版本列表。系统显示文件的所有历史版本及修改时间。用户可以选择恢复到某个历史版本。
- **恢复文件版本**：用户选择某个历史版本进行恢复。系统执行恢复操作并更新文件信息数据库。

##### 搜索和预览模块
- **文件搜索**：用户输入关键词搜索文件和文件夹。系统根据关键词在文件信息数据库中检索匹配结果。显示匹配的文件列表，支持高级搜索和全文搜索。
- **文件预览**：用户选择文件进行在线预览。系统支持常见文件格式（图片、视频、PDF、文档等）的在线预览。

##### 日志管理模块
- **用户操作日志**：系统记录用户操作（上传、下载、删除、共享等）日志。管理员可以查看和查询操作日志，支持日志审计。
- **系统运行日志**：系统记录服务器状态、错误日志等信息。管理员可以查看和分析系统运行日志，进行故障排查和系统维护。

##### 通知提醒模块
- **操作完成通知**：文件上传和下载完成后，系统发送通知提醒用户。用户可以在通知中心查看操作完成消息。
- **共享链接续期提醒**：系统在共享链接即将到期时发送提醒通知。用户可以选择续期或取消共享，系统更新共享信息。

##### 系统管理模块
- **数据备份和恢复**：系统定期进行数据备份，确保数据安全。管理员可以执行数据恢复操作，恢复丢失的数据。
- **文件审核**：管理员审核用户上传的文件，确保不包含违规内容。支持删除或屏蔽违规文件，并通知用户审核结果。
- **存储监控**：管理员监控文件存储使用情况，优化存储资源。设置存储警告阈值，在存储空间不足时发送警告通知。