package com.inspiredandroid.kai.build

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Developer CLI tools & environments available for installation in Ari Build Debian sandbox.
 */
@Immutable
data class BuildAgent(
    val id: String,
    val title: String,
    val binary: String,
    val installCommand: String,
)

object BuildAgents {

    val all: ImmutableList<BuildAgent> = persistentListOf(
        BuildAgent(
            id = "node-toolchain",
            title = "Node.js & Tools (npm/pnpm/yarn)",
            binary = "node",
            installCommand = "apt-get update && apt-get install -y nodejs npm && npm install -g pnpm yarn && ln -sf /usr/bin/node /usr/local/bin/node 2>/dev/null || true",
        ),
        BuildAgent(
            id = "python-uv",
            title = "Python 3 & Astral UV",
            binary = "uv",
            installCommand = "apt-get update && apt-get install -y python3 python3-pip python3-venv && curl -fsSL https://astral.sh/uv/install.sh | bash && (ln -sf /root/.cargo/bin/uv /usr/local/bin/uv 2>/dev/null || ln -sf /root/.local/bin/uv /usr/local/bin/uv 2>/dev/null || true)",
        ),
        BuildAgent(
            id = "gh",
            title = "GitHub CLI (gh)",
            binary = "gh",
            installCommand = "mkdir -p -m 755 /etc/apt/keyrings && curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | tee /etc/apt/keyrings/githubcli-archive-keyring.gpg > /dev/null && chmod go+r /etc/apt/keyrings/githubcli-archive-keyring.gpg && echo 'deb [arch=arm64,amd64 signed-by=/etc/apt/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main' | tee /etc/apt/sources.list.d/github-cli.list > /dev/null && apt-get update && apt-get install -y gh",
        ),
        BuildAgent(
            id = "aider",
            title = "Aider AI Pair Programmer",
            binary = "aider",
            installCommand = "apt-get update && apt-get install -y python3-pip python3-venv git curl && pip3 install --break-system-packages --upgrade aider-chat && ln -sf /root/.local/bin/aider /usr/local/bin/aider 2>/dev/null || true",
        ),
        BuildAgent(
            id = "term-essentials",
            title = "Terminal Power Tools (tmux, htop, ripgrep, fzf, jq)",
            binary = "tmux",
            installCommand = "apt-get update && apt-get install -y tmux htop ripgrep fzf jq tree curl wget git nano neovim bsdmainutils",
        ),
    )

    fun get(id: String?): BuildAgent? = all.firstOrNull { it.id == id }
}
