### git mac安装（window系统，Linux系统差不多都是类似）
我个人开始入门的时候还是喜欢各种花式界面的，但是后面还是觉得命令行比较让人喜欢

* 输入git 直接直接提示安装
* 输入密码之后安装完成
* 在想要clone的目录下面创建文件夹
```
When you git clone, git fetch, git pull, or git push to a remote repository using HTTPS URLs on the command line, 
Git will ask for your GitHub username and password. When Git prompts you for your password, 
enter your personal access token (PAT) instead. Password-based authentication for Git has been removed, 
and using a PAT is more secure. For more information, see "Creating a personal access token."
```

```
#克隆代码下来
git clone https://github.com/Teahel/JavaLine.git

# 更新代码
git pull , git fetch

#添加文件（名字过长按tab键可以填充）
git add * /"具体文件名"

#提交
git commit -m "提交描述"

```
