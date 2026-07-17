# KoogStudio Design Rules

## Core Principle

Use Material3 defaults as-is. No custom theming, no overrides, no hardcoding.

## Colors

- Use ONLY `MaterialTheme.colorScheme.*` tokens
- Never define custom Color() values
- Never use hex codes or RGB literals
- Example: `MaterialTheme.colorScheme.primary`, `MaterialTheme.colorScheme.surface`

## Typography

- Use ONLY `MaterialTheme.typography.*` styles
- Never define custom TextStyle() or FontSize values
- Example: `MaterialTheme.typography.titleLarge`, `MaterialTheme.typography.bodyMedium`

## Shapes

- Use ONLY `MaterialTheme.shapes.*` tokens
- Never define custom RoundedCornerShape or corner values
- Example: `MaterialTheme.shapes.medium`, `MaterialTheme.shapes.large`

## Components

- Use Material3 composable defaults as provided
- Do not override `modifier = Modifier.size()` on standard components unless layout requires it
- Use built-in variants: `Button`, `OutlinedButton`, `TextButton`, `IconButton`
- Use `Scaffold`, `TopAppBar`, `NavigationBar`, `Card`, `ListItem` with defaults

## Spacing

- Use `MaterialTheme.spacing` or consistent padding values
- Standard padding increments: 4, 8, 12, 16, 24, 32
- Do not invent custom spacing tokens

## Icons

- NEVER use `Icons.*` from any Compose icon library
- Copy required SVG icons from `/Users/raju/Documents/material svg icons` into the project
- Store icons under `shared/src/commonMain/composeResources/drawable/`
- Load via `painterResource(Res.drawable.icon_name)`
- Source folder structure mirrors categories: `action/`, `content/`, `navigation/`, etc.
- Keep icon filenames as-is from source (snake_case)

## Layout

- One Scaffold per screen
- TopAppBar for navigation/title
- Content area with standard padding
- No custom layout composables unless absolutely necessary

## File Structure

```
ui/
├── theme/
│   └── Theme.kt          ← only wraps MaterialTheme(), no overrides
├── screens/              ← one composable per screen
└── components/           ← only if shared across multiple screens
```

## What NOT To Do

- No `Color(0xFF...)` anywhere
- No custom `Typography()` or `Shapes()` definitions
- No `Modifier.background(Color(...))` 
- No `TextStyle(fontSize = X.dp)` with hardcoded values
- No custom `RoundedCornerShape(X.dp)`
- No `Icons.*` imports — always use copied SVGs from `/Users/raju/Documents/material svg icons`
