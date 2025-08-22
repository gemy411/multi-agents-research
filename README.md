# Multi-Agent Research System

This project implements a sophisticated multi-agent research system based on the concepts described in Anthropic's article: [Building a Multi-Agent Research System](https://www.anthropic.com/engineering/built-multi-agent-research-system). The system leverages the power of coordinated AI agents to handle deep and complex research queries that require multiple perspectives and iterative investigation.

## Overview

The system is built around a **Lead Research Agent** that orchestrates the entire research process. This lead agent has access to a powerful `deployAgent` tool that can dynamically create specialized **Research Subagents** to handle specific aspects of complex queries. The lead agent can deploy multiple subagents simultaneously and can choose to deploy additional subagents after each iteration based on the evolving needs of the research task.

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
              │  ┌─────────────────────┐  │
              │  │   completeTask      │  │
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

**Key Features:**
- Uses Gemini Pro model for advanced reasoning
- Supports up to 50 iterations for complex tasks
- Parallel tool execution capabilities
- Dynamic subagent deployment and management

### Research Subagents

**Research Subagents** are specialized agents created by the lead agent to handle specific research tasks. Each subagent:

- **Focuses** on a specific aspect of the research query
- **Searches** the web using natural language queries
- **Fetches** detailed content from specific URLs
- **Reports** findings back to the lead agent

**Key Features:**
- Uses Gemini Flash model for efficient processing
- Optimized for focused research tasks (30 iterations max)
- Equipped with web search and content fetching tools
- Unique identification for tracking and coordination

### Agent Workflow

1. **Query Reception**: Complex research query received via MCP or REST API
2. **Analysis**: Lead agent analyzes the query and creates research strategy
3. **Deployment**: Lead agent deploys initial set of subagents with specific tasks
4. **Parallel Research**: Multiple subagents work simultaneously on different aspects
5. **Progress Evaluation**: Lead agent evaluates intermediate results
6. **Iterative Deployment**: Additional subagents deployed if needed
7. **Synthesis**: Lead agent combines all findings into comprehensive answer
8. **Response**: Final research results returned to client

## Technology Stack

### Koog Framework
This implementation is built using the [Koog AI Framework](https://docs.koog.ai/), a Kotlin-based framework for building sophisticated AI agent systems. Koog provides:

- **Agent Management**: Core agent lifecycle and execution management
- **Tool Registry**: Dynamic tool registration and execution
- **Strategy Patterns**: Declarative agent behavior definition
- **Prompt Management**: Sophisticated prompt engineering capabilities
- **Model Integration**: Support for multiple LLM providers

### Key Dependencies
- **Koog Agents Core**: `ai.koog.agents.core.*` - Main agent framework
- **Koog Prompts**: `ai.koog.prompt.dsl.*` - Prompt engineering tools
- **Ktor**: Web server and HTTP client functionality
- **Model Context Protocol**: MCP integration for external tool access
- **OpenRouter**: LLM model access and execution

## API Interfaces

The system exposes its functionality through two interfaces:

### 1. Model Context Protocol (MCP)
The system implements MCP to expose the `DeepResearch` tool, allowing integration with MCP-compatible clients:

```json
{
  "tool": "DeepResearch",
  "arguments": {
    "query": "Your complex research question here"
  }
}
```

### 2. REST API
A standard HTTP API is also available:

```bash
GET /search?query=Your%20complex%20research%20question%20here
```

Both interfaces use the same underlying Lead Research Agent and provide identical functionality.

## Usage Examples

### Complex Research Query
```
Query: "Compare the latest developments in quantum computing approaches by major tech companies and analyze their potential impact on cryptography"

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
5. Final synthesis into comprehensive report
```

### Multi-Domain Investigation
```
Query: "Investigate the economic, environmental, and social impacts of vertical farming adoption in urban areas"

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

### Environment Setup
Configure your environment variables by copying the example environment file:
```bash
# Copy the example environment file
cp example.env .env

# Edit the .env file with your API keys
# The .env file should contain:
OPEN_ROUTER_API_KEY="your_api_key_here"
```

### Building & Running

```bash
# Build the project
./gradlew build

# Run the server
./gradlew run

```

### Server Startup
When the server starts successfully, you'll see:
```
Application started in 0.303 seconds.
Responding at http://0.0.0.0:8080
```

## Configuration

### Agent Configuration
- **Lead Agent**: Gemini Pro model, 50 max iterations, history compression
- **Subagents**: Gemini Flash model, 30 max iterations, focused tools
- **Parallel Execution**: Enabled for both agent types
- **Tool Registry**: Dynamic tool registration and management

### Search Configuration
- **Search Provider**: Sonar Search Manager (using Sonar by Perplexity model)
- **Model Selection**: OpenRouter is used across the entire application to access different models, including the Sonar by Perplexity model for search functionality
- **Content Fetching**: Content extraction from web pages
- **Natural Language Results**: Processed search results in conversational format

## Development

### Project Structure (Non-Final)
```
src/..
├── agents/
│   ├── lead/              # Lead Research Agent implementation
│   ├── subagent/          # Research Subagent implementation
│   ├── search/            # Search functionality
│   ├── models/            # LLM model configurations
│   └── prompts/           # Prompt templates and factory
├── mcp/                   # Model Context Protocol integration
├── Application.kt         # Main application entry point
└── Routing.kt            # API routing configuration
```

### Key Classes
- `LeadResearchAgent`: Main orchestrating agent
- `ResearchSubagent`: Specialized research agents
- `ResearchLeadAgentTools`: Tools available to lead agent (deployAgent, completeTask)
- `ResearchSubagentTools`: Tools available to subagents (webSearch, webFetch)
- `SonarSearchManager`: Search and content fetching implementation

**Note:** The complete task tool is now useless.
## Contributing

This project demonstrates multi-agent coordination and can be extended with:
- Optimizing parallel processing
- Manage agent memory to save context window 
- Enhanced search capabilities
- Integration with more LLM providers
- Functional citation management
- Performance monitoring and analytics

**For more details check the [Roadmap.md](Roadmap.md)**

---

*Built with the [Koog AI Framework](https://docs.koog.ai/) - A powerful Kotlin framework for building sophisticated AI agent systems.*

