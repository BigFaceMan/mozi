import torch
import torch.nn as nn
import torch.optim as optim
import torchvision
import torchvision.transforms as transforms
from torch.utils.data import DataLoader

# 定义一个简单的 CNN 模型
class Model(nn.Module):
    def __init__(self):
        super(SimpleCNN, self).__init__()

        # 定义卷积层
        self.conv1 = nn.Conv2d(3, 32, kernel_size=3, padding=1)  # 输入3通道（RGB），输出32通道，卷积核大小3x3
        self.conv2 = nn.Conv2d(32, 64, kernel_size=3, padding=1)  # 输入32通道，输出64通道，卷积核大小3x3

        # 定义池化层
        self.pool = nn.MaxPool2d(kernel_size=2, stride=2)  # 最大池化层，池化核大小2x2

        # 定义全连接层
        self.fc1 = nn.Linear(64 * 8 * 8, 512)  # 输入64*8*8，输出512
        self.fc2 = nn.Linear(512, 10)  # 输出10个类别

    def forward(self, x):
        # 定义前向传播过程
        x = self.pool(torch.relu(self.conv1(x)))  # 先经过卷积层1，再进行ReLU激活和池化
        x = self.pool(torch.relu(self.conv2(x)))  # 经过卷积层2、ReLU激活和池化
        x = x.view(-1, 64 * 8 * 8)  # 展平为一维向量
        x = torch.relu(self.fc1(x))  # 经过全连接层1和ReLU激活
        x = self.fc2(x)  # 最后输出层
        return x


