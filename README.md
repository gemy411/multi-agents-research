# Multi-Agent Research System

A sophisticated multi-agent research system inspired by Anthropic's article: [Building a Multi-Agent Research System](https://www.anthropic.com/engineering/built-multi-agent-research-system). It uses coordinated AI agents to handle deep, complex research queries that require multiple perspectives and iterative investigation.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [API Interfaces](#api-interfaces)
- [Usage Examples](#usage-examples)
- [Installation & Running](#installation--running)
- [Configuration](#configuration)
- [Development](#development)
- [Contributing](#contributing)

## Overview

The system is built around a **Lead Research Agent** that orchestrates the entire research process. Using a `deployAgent` tool, it dynamically creates specialized **Research Subagents** to handle specific aspects of complex queries. The lead agent can deploy multiple subagents simultaneously and spawn additional ones as research evolves.

This multi-agent architecture enables the system to:

- Break down complex queries into manageable sub-tasks
- Perform parallel research across multiple domains
- Iterate and refine research based on intermediate findings
- Synthesize results from multiple specialized agents

## Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────────┐
│                          Client Request                          │
└─────────────────────┬───────────────────┬───────────────────────┘
                      │                   │
               ┌──────▼──────┐     ┌──────▼──────┐
               │  MCP Tool   │     │ REST API    │
               │"DeepResearch"│     │ /search     │
               └──────┬──────┘     └──────┬──────┘
                      │                   │
                      └──────┬────────────┘
                             │
               ┌─────────────▼─────────────┐
               │    Lead Research Agent    │
               │  ┌─────────────────────┐  │
               │  │   deployAgent Tool  │  │
               │  └─────────────────────┘  │
               └─────────────┬─────────────┘
                            │
           ┌────────────────┼────────────────┐
           │                │                │
     ┌─────▼─────┐   ┌─────▼─────┐   ┌─────▼─────┐
     │ Subagent  │   │ Subagent  │   │ Subagent  │
     │     1     │   │     2     │   │     N     │
     │ ┌───────┐ │   │ ┌───────┐ │   │ ┌───────┐ │
     │ │Search │ │   │ │Search │ │   │ │Search │ │
     │ └───────┘ │   │ └───────┘ │   │ └───────┘ │
     │ ┌───────┐ │   │ ┌───────┐ │   │ ┌───────┐ │
     │ │ Fetch │ │   │ │ Fetch │ │   │ │ Fetch │ │
     │ └───────┘ │   │ └───────┘ │   │ └───────┘ │
     └───────────┘   └───────────┘   └───────────┘
           │               │               │
           └───────────────┼───────────────┘
                           │
               ┌───────────▼───────────┐
               │   Sonar Search        │
               │   Manager             │
               │  ┌─────────────────┐  │
               │  │  Web Search     │  │
               │  │  Content Fetch  │  │
               │  └─────────────────┘  │
               └───────────────────────┘
```

### Lead Research Agent

The **Lead Research Agent** is the orchestrator of the entire system. It:

- **Analyzes** incoming complex queries and determines the best approach
- **Deploys** multiple subagents using the `deployAgent` tool
- **Coordinates** parallel research activities
- **Synthesizes** results from all subagents into comprehensive answers
- **Iterates** by deploying additional subagents based on intermediate findings
- **Manages** conversation history and compresses it when needed (>20 messages)

| Feature | Detail |
|---|---|
| Model | Gemini Pro |
| Max Iterations | 50 |
| Parallel Execution | Enabled |
| History Compression | Yes (>20 messages) |

### Research Subagents

**Research Subagents** are specialized agents created by the lead agent to handle specific research tasks. Each subagent:

- **Focuses** on a specific aspect of the research query
- **Searches** the web using natural language queries
- **Fetches** detailed content from specific URLs
- **Reports** findings back to the lead agent

| Feature | Detail |
|---|---|
| Model | Gemini Flash |
| Max Iterations | 30 |
| Parallel Execution | Enabled |
| Identification | Unique ID per agent |

### Agent Workflow

```
1. Query Reception   →  Complex query received via MCP or REST API
2. Analysis          →  Lead agent analyzes and creates a research strategy
3. Deployment        →  Lead agent deploys initial set of subagents
4. Parallel Research →  Subagents work simultaneously on different aspects
5. Evaluation        →  Lead agent evaluates intermediate results
6. Iteration         →  Additional subagents deployed if needed
7. Synthesis         →  Lead agent combines all findings
8. Response          →  Final results returned to client
```

## Technology Stack

### Koog Framework

Built using the [Koog AI Framework](https://docs.koog.ai/), a Kotlin-based framework for building sophisticated AI agent systems. Koog provides:

- **Agent Management**: Core agent lifecycle and execution management
- **Tool Registry**: Dynamic tool registration and execution
- **Strategy Patterns**: Declarative agent behavior definition
- **Prompt Management**: Sophisticated prompt engineering capabilities
- **Model Integration**: Support for multiple LLM providers

### Key Dependencies

| Dependency | Purpose |
|---|---|
| `ai.koog.agents.core.*` | Main agent framework |
| `ai.koog.prompt.dsl.*` | Prompt engineering tools |
| Ktor | Web server and HTTP client |
| Model Context Protocol | MCP integration |
| OpenRouter | LLM model access |

## API Interfaces

The system exposes its functionality through two interfaces:

### 1. Model Context Protocol (MCP)

Implements MCP to expose the `DeepResearch` tool for integration with MCP-compatible clients:

```json
{
  "tool": "DeepResearch",
  "arguments": {
    "query": "Your complex research question here"
  }
}
```

### 2. REST API

A standard HTTP endpoint is also available:

```bash
GET /search?query=Your%20complex%20research%20question%20here
```

Both interfaces use the same underlying Lead Research Agent and provide identical functionality.

## Usage Examples

### Complex Research Query

```
Query: "Compare the latest developments in quantum computing approaches by major
        tech companies and analyze their potential impact on cryptography"

Process:
1. Lead agent analyzes the multi-faceted query
2. Deploys subagents for:
   - IBM quantum computing developments
   - Google quantum computing research
   - Microsoft quantum initiatives
   - Quantum cryptography implications
3. Subagents perform parallel research
4. Lead agent evaluates findings and deploys additional subagents for:
   - Timeline comparisons
   - Technical depth analysis
5. Final synthesis into a comprehensive report
```

### Multi-Domain Investigation

```
Query: "Investigate the economic, environmental, and social impacts of
        vertical farming adoption in urban areas"

Process:
1. Lead agent identifies three main research domains
2. Simultaneously deploys subagents for:
   - Economic analysis and market data
   - Environmental impact studies
   - Social and community effects
3. Each subagent conducts focused research in their domain
4. Lead agent synthesizes cross-domain insights
5. Additional subagents deployed for comparative analysis
```

## Installation & Running

### Prerequisites

- JDK 17+
- An [OpenRouter](https://openrouter.ai/) API key

### Environment Setup

```bash
# Copy the example environment file
cp example.env .env
```

Then edit `.env` with your credentials:

```env
OPEN_ROUTER_API_KEY="your_api_key_here"
PORT=4321  # Optional — defaults to 8080
```

### Build & Run

```bash
# Build the project
./gradlew build

# Run the server
./gradlew run
```

When the server starts successfully, you'll see:

```
Application started in 0.303 seconds.
Responding at http://0.0.0.0:8080
```

### Docker

```bash
docker compose up
```

## Configuration

### Agent Configuration

| Setting | Lead Agent | Subagents |
|---|---|---|
| Model | Gemini Pro | Gemini Flash |
| Max Iterations | 50 | 30 |
| Parallel Execution | Yes | Yes |
| History Compression | Yes (>20 msgs) | No |

### Search Configuration

- **Search Provider**: Sonar Search Manager (Sonar by Perplexity via OpenRouter)
- **Content Fetching**: Extracts full content from web pages
- **Result Format**: Natural language, conversational output

## Development

### Project Structure

> **Note:** The project structure is not yet finalized and may change.

```
src/
├── agents/
│   ├── lead/              # Lead Research Agent implementation
│   ├── subagent/          # Research Subagent implementation
│   ├── search/            # Search functionality
│   ├── models/            # LLM model configurations
│   └── prompts/           # Prompt templates and factory
├── mcp/                   # Model Context Protocol integration
├── Application.kt         # Main application entry point
└── Routing.kt             # API routing configuration
```

### Key Classes

| Class | Responsibility |
|---|---|
| `LeadResearchAgent` | Main orchestrating agent |
| `ResearchSubagent` | Specialized research agents |
| `ResearchLeadAgentTools` | Tools for lead agent (`deployAgent`) |
| `ResearchSubagentTools` | Tools for subagents (`webSearch`, `webFetch`) |
| `SonarSearchManager` | Search and content fetching implementation |

## Contributing

This project demonstrates multi-agent coordination and welcomes contributions in areas such as:

- Optimizing parallel processing
- Agent memory management to reduce context window usage
- Enhanced search capabilities
- Integration with additional LLM providers
- Functional citation management
- Performance monitoring and analytics

For planned improvements, see the [Roadmap](ROADMAP.md).

---

*Built with the [Koog AI Framework](https://docs.koog.ai/) — a powerful Kotlin framework for building sophisticated AI agent systems.*
