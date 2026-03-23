#!/bin/bash
# Run from ide-plugin/ directory.
# Tags use the "plugin/v<version>" prefix to coexist with mobile-app tags in the monorepo.
set -e

COMMAND=${1:-"help"}

case "$COMMAND" in
  # Preview what is batched in [Unreleased] since the last tag
  "preview")
    echo "=== Unreleased changes ==="
    git cliff --config cliff.toml --unreleased --strip header
    ;;

  # Update CHANGELOG.md -> add/update section [Unreleased]
  "update")
    git cliff --config cliff.toml --unreleased --prepend CHANGELOG.md
    echo "✓ CHANGELOG.md updated with unreleased commits"
    ;;

  # Roll a release: update CHANGELOG.md with version and create a tag.
  # Usage: ./scripts/changelog.sh release 1.2.3
  "release")
    VERSION=${2:?"Usage: $0 release <version>"}
    TAG="plugin/v$VERSION"

    # 1. Bump plugin.version in gradle.properties
    sed -i '' "s/^plugin\.version=.*/plugin.version=$VERSION/" gradle.properties
    echo "✓ plugin.version=$VERSION in gradle.properties"

    # 2. git-cliff generates the final changelog for the current version
    git cliff --config cliff.toml --tag "$TAG" --prepend CHANGELOG.md
    echo "✓ CHANGELOG.md updated for $TAG"

    # 3. Commit changes
    git add CHANGELOG.md gradle.properties
    git commit -m "chore(release): prepare for plugin/v$VERSION"

    # 4. Create annotated tag
    git tag -a "$TAG" -m "Release $TAG"
    echo "✓ Tag $TAG created"

    echo ""
    echo "Next steps:"
    echo "  git push origin main --tags"
    echo "  cd ide-plugin && ./gradlew buildPlugin publishPlugin"
    ;;

  "help"|*)
    echo "Usage: $0 <command>"
    echo ""
    echo "Commands:"
    echo "  preview            Show unreleased commits for ide-plugin/"
    echo "  update             Prepend unreleased section to CHANGELOG.md"
    echo "  release <ver>      Cut a release: update changelog, bump version, create tag"
    echo ""
    echo "Tags use the 'plugin/v<ver>' prefix (e.g. plugin/v1.2.3)."
    ;;
esac
