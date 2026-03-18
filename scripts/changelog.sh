#!/bin/bash
set -e

COMMAND=${1:-"help"}

case "$COMMAND" in
  # Показать что накопилось в [Unreleased] с момента последнего тега
  "preview")
    echo "=== Unreleased changes ==="
    git cliff --unreleased --strip header
    ;;

  # Обновить CHANGELOG.md — добавить/обновить секцию [Unreleased]
  "update")
    git cliff --unreleased --prepend CHANGELOG.md
    echo "✓ CHANGELOG.md updated with unreleased commits"
    ;;

  # Выпустить релиз: обновить CHANGELOG.md с версией и создать тег
  # Использование: ./scripts/changelog.sh release 1.0.0
  "release")
    VERSION=${2:?"Usage: $0 release <version>"}

    # 1. Обновляем gradle.properties
    sed -i '' "s/^pluginVersion=.*/pluginVersion=$VERSION/" gradle.properties
    echo "✓ pluginVersion=$VERSION in gradle.properties"

    # 2. git-cliff генерирует финальный changelog для этой версии
    git cliff --tag "v$VERSION" --prepend CHANGELOG.md
    echo "✓ CHANGELOG.md updated for v$VERSION"

    # 3. Коммитим изменения
    git add CHANGELOG.md gradle.properties
    git commit -m "chore: release v$VERSION"

    # 4. Создаём тег
    git tag -a "v$VERSION" -m "Release v$VERSION"
    echo "✓ Tag v$VERSION created"

    echo ""
    echo "Next steps:"
    echo "  git push origin main --tags"
    echo "  ./gradlew buildPlugin publishPlugin"
    ;;

  "help"|*)
    echo "Usage: $0 <command>"
    echo ""
    echo "Commands:"
    echo "  preview          Show unreleased commits that would go into changelog"
    echo "  update           Prepend unreleased section to CHANGELOG.md"
    echo "  release <ver>    Cut a release: update changelog, bump version, create tag"
    ;;
esac