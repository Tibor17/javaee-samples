# Install VirtualBox 5.0.26 x64 
Download VirtualBox from Oracle.com.    
You must have x64 bit host OS (Windows 7 x64) and x64 bit VirtualBox.


# Download Fedora 23 (64bit).vdi
Download 64-bit VDI image of Fedora 23 Desktop for Virtual Box VM   
[http://www.osboxes.org/fedora/](http://www.osboxes.org/fedora/)

Install the image in VirtualBox and login in Fedora.

Username: osboxes   
Password: osboxes.org


# Update Fedora Guest OS    
Make sure your existing dnf packages are up-to-date.    
`$ sudo dnf update`


# Add the `yum` repo    
```shell
[osboxes@osboxes ~]$ sudo tee /etc/yum.repos.d/docker.repo <<-'EOF'  
> [dockerrepo]  
> name=Docker Repository    
> baseurl=https://yum.dockerproject.org/repo/main/fedora/23/    
> enabled=1 
> gpgcheck=1    
> gpgkey=https://yum.dockerproject.org/gpg  
> EOF
```    


# Install the Docker package
`$ sudo dnf install docker-engine`


# Enable the socket and service
`$ sudo systemctl enable docker.socket docker.service`


# Start the Docker daemon
`$ sudo systemctl start docker`


# Verify `docker` is installed correctly by running a test image `hello-world` in a container
`$ sudo docker run hello-world`     
_Hello from Docker._    
This message shows that your installation appears to be working correctly.


# Folders shared by Guest OS and Host OS
Install GCC and kernel-devel.   
`$ sudo dnf install gcc kernel-devel`

Open the "Oracle VM VirtualBox Manager" and open "Shared folders" in your VM (Fedora 23).   
"Add Share":    
Folder Path: D:\public  
Folder Name: public     
Auto-mount, Make permanent.     

Go to the window of VM (Fedora 23 [Running] Oracle VM VirtualBox). Click on the "Devices" menu and select the   
"Insert Guest Additions CD image...". This is _VBoxGuestAdditions.iso_ in installation folder of VirtualBox.    

Reboot the system and configure shared folders. 

If you have not installed GCC and kernel, the VBoxManager fails to mount shared folders.    
Then you should install both and setup VBox addons:     
`$ cd /opt/VBoxGuestAdditions-5.0.26/init`  
`$ sudo ./vboxadd setup`    

List all files in shared folder:    
`$ su`  
`$ ls /media/sf_public`     


# References
[Docker on Fedora](https://docs.docker.com/engine/installation/linux/fedora/)   
[Upgrade Docker on Fedora 23](http://www.liquidweb.com/kb/how-to-updateupgrade-docker-on-fedora-23/)    
[Shared folders in Fedora VM VirtualBox](https://wikisree.com/2015/03/21/shared-folder-for-fedora-vm-in-virtualbox/)    
[Mount Failed vboxsf](http://stackoverflow.com/questions/28328775/virtualbox-mount-vboxsf-mounting-failed-with-the-error-no-such-device)    
[Release Ubuntu Xenial](http://releases.ubuntu.com/16.04/)  
[Docker on Ubuntu Xenial](https://apt.dockerproject.org/repo/dists/ubuntu-xenial)   
[Docker on Ubuntu](https://docs.docker.com/engine/installation/linux/ubuntulinux/)  
[Installing VirtualBox Ubuntu and Docker](https://pods.iplantcollaborative.org/wiki/display/HDFDE/Installing+VirtualBox,+Ubuntu,+and+Docker)    
