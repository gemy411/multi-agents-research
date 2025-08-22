# Contributing to Research Agents

Thank you for your interest in contributing to Research Agents! This project aims to build a sophisticated multi-agent research system, and we welcome contributions from the community.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How to Contribute](#how-to-contribute)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Reporting Issues](#reporting-issues)
- [Questions and Support](#questions-and-support)

## Code of Conduct

This project adheres to a Code of Conduct that we expect all contributors to follow. Please read [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing.

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/your-username/research-agents.git
   cd research-agents
   ```
3. Set up the development environment (see [Development Setup](#development-setup))
4. Create a new branch for your contribution:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## How to Contribute

### Types of Contributions

We welcome various types of contributions:

- **Bug fixes**: Help us identify and fix issues
- **Feature development**: Implement new functionality from our [roadmap](ROADMAP.md)
- **Documentation**: Improve existing docs or create new ones
- **Testing**: Add or improve test coverage
- **Performance optimizations**: Help make the system more efficient
- **Code quality improvements**: Refactoring, better error handling, etc.

### Priority Areas

Based on our [roadmap](ROADMAP.md), we're particularly interested in contributions for:

- **Parallel Processing Optimization**: Improve time-travel mechanisms and progress monitoring
- **Memory Management**: Optimize agent information storage and retrieval
- **Citation System**: Develop robust source tracking and credibility frameworks
- **Local Machine Scraping**: MCP server integration for cost-free web fetching
- **Architectural Improvements**: Better component organization and scalability

## Development Setup

### Prerequisites

- **Java 17 or higher**
- **Kotlin 1.9+**
- **Docker** (for containerized development)
- **Git**

### Environment Setup

1. **Clone and setup**:
   ```bash
   git clone https://github.com/your-username/research-agents.git
   cd research-agents
   ```

2. **Configure environment**:
   ```bash
   cp example.env .env
   # Edit .env with your API keys and configuration
   ```

3. **Build the project**:
   ```bash
   ./gradlew build
   ```

4. **Run tests**:
   ```bash
   ./gradlew test
   ```

5. **Start the application**:
   ```bash
   ./gradlew run
   ```

### Docker Development

```bash
# Build and run with Docker Compose
docker-compose up --build

# Or build the Docker image manually
docker build -t research-agents .

# Create your .env file from the example first
cp example.env .env
# Edit .env with your actual values

# Run with env file host bind (required for environment variables)
docker run -p 8080:8080 --env-file .env research-agents
```

## Project Structure

```
src/main/kotlin/com/gemy/
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

### Key Components

- **LeadResearchAgent**: Main orchestrating agent
- **ResearchSubagent**: Specialized research agents
- **SonarSearchManager**: Search and content fetching
- **Tool Systems**: Agent-specific tool implementations

## Coding Standards

### Kotlin Style Guide

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions focused and small
- Use data classes for simple data holders

### Code Quality

- **No warnings**: Code should compile without warnings
- **Null safety**: Leverage Kotlin's null safety features
- **Immutability**: Prefer immutable data structures when possible
- **Error handling**: Use proper exception handling and meaningful error messages
- **Logging**: Use structured logging with appropriate levels

### Testing

- Write unit tests for new functionality
- Maintain or improve test coverage
- Use descriptive test method names
- Test both happy path and edge cases
- Mock external dependencies appropriately

## Commit Guidelines

### Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, etc.)
- **refactor**: Code refactoring
- **test**: Adding or updating tests
- **chore**: Maintenance tasks

### Examples

```
feat(agents): add parallel processing optimization for subagents

Implement time-travel mechanisms and progress monitoring to improve
computational resource utilization during multi-agent research tasks.

Closes #42
```

```
fix(search): handle empty search results gracefully

Previously, the system would throw an exception when no search results
were found. Now it returns an appropriate response with helpful messaging.

Fixes #38
```

## Pull Request Process

1. **Update documentation**: Ensure README.md and other docs reflect your changes
2. **Add tests**: Include appropriate test coverage for new functionality
3. **Update changelog**: Add your changes to CHANGELOG.md
4. **Ensure CI passes**: All automated checks must pass
5. **Request review**: Tag relevant maintainers for review

### Pull Request Template

When creating a pull request, please include:

- **Description**: Clear explanation of changes made
- **Motivation**: Why the changes are needed
- **Testing**: How the changes were tested
- **Screenshots**: If applicable, for UI changes
- **Breaking changes**: Any backward compatibility considerations
- **Related issues**: Link to related GitHub issues

## Reporting Issues

### Before Reporting

1. Check existing issues to avoid duplicates
2. Test with the latest version
3. Gather relevant information (logs, environment details, etc.)

### Issue Template

When reporting issues, please include:

- **Environment**: OS, Java version, Kotlin version
- **Steps to reproduce**: Clear, minimal reproduction steps
- **Expected behavior**: What you expected to happen
- **Actual behavior**: What actually happened
- **Logs**: Relevant error messages or logs
- **Additional context**: Any other relevant information

### Security Issues

For security vulnerabilities, please see our [Security Policy](SECURITY.md) and report them privately.

## Questions and Support

- **GitHub Discussions**: For general questions and community discussions
- **GitHub Issues**: For bug reports and feature requests
- **Documentation**: Check our [README](README.md) and [roadmap](ROADMAP.md)

## Recognition

Contributors will be recognized in our:
- **Contributors list** in README.md
- **Release notes** for significant contributions
- **Special mentions** for outstanding contributions

## License

By contributing to Research Agents, you agree that your contributions will be licensed under the [MIT License](LICENSE.txt).

---

Thank you for contributing to Research Agents! Your efforts help make this project better for everyone.