package org.example.backend;

import org.example.backend.mapper.UserMapper;
import org.example.backend.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.List;

@SpringBootTest
class BackendApplicationTests {
    @Autowired
    private UserMapper userMapper;
    @Test
    void contextLoads() {

        String code =
                "import torch\n" +
                        "import torch.nn as nn\n" +
                        "\n" +
                        "class Model(nn.Module):\n" +
                        "    def __init__(self, input_size, hidden_size, output_size):\n" +
                        "        super(Model, self).__init__()\n" +
                        "        self.model = nn.Sequential(\n" +
                        "            nn.Linear(input_size, hidden_size),\n" +
                        "            nn.ReLU(),\n" +
                        "            nn.Linear(hidden_size, output_size)\n" +
                        "        )\n" +
                        "\n" +
                        "    def forward(self, x):\n" +
                        "        return self.model(x)\n" +
                        "\n" +
                        "# Example instantiation\n" +
                        "# model = Model(input_size=784, hidden_size=128, output_size=10)\n";
    }

}
