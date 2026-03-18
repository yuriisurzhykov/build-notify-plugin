#!/bin/bash
set -e

COMMAND=${1:-"help"}

case "$COMMAND" in
  # Preview what is batched in [Unreleased] since the last tag
  "preview")
    echo "=== Unreleased changes ==="
    git cliff --unreleased --strip header
    ;;

  # Update CHANGELOG.md -> add/update section [Unreleased]
  "update")
    git cliff --unreleased --prepend CHANGELOG.md
    echo "✓ CHANGELOG.md updated with unreleased commits"
    ;;

  # Roll a release: update CHANGELOG.md with version and create a tag.
  # Usage: ./scripts/changelog.sh release 1.0.0
  "release")
    VERSION=${2:?"Usage: $0 release <version>"}

    # 1. Updating gradle.properties
    sed -i '' "s/^pluginVersion=.*/pluginVersion=$VERSION/" gradle.properties
    echo "✓ pluginVersion=$VERSION in gradle.properties"

    # 2. git-cliff generates the final changelog for the current version
    git cliff --tag "v$VERSION" --prepend CHANGELOG.md
    echo "✓ CHANGELOG.md updated for v$VERSION"

    # 3. Commit changes
    git add CHANGELOG.md gradle.properties
    git commit -m "chore: release v$VERSION"

    # 4. Create a tag
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