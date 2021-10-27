
* curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun

* sudo yum install -y yum-utils \
  device-mapper-persistent-data \
  lvm2

* sudo yum-config-manager \
    --add-repo \
    http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo


* sudo yum install docker-ce docker-ce-cli containerd.io
