//! In-memory electrical symbol catalog and search contracts.

mod catalog;
mod search;

pub use catalog::{
    CatalogError, PlacementPreview, PlacementTerminalAnchor, Primitive, SymbolCatalog,
    SymbolDefinitionRecord, TerminalTemplate,
};
pub use search::{SearchQuery, SearchResult};

/// Compile-time marker for the library crate.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct LibraryContract;

#[cfg(test)]
mod tests {
    use super::LibraryContract;

    #[test]
    fn workspace_crates_compile_contract() {
        let marker = LibraryContract;
        assert_eq!(marker, LibraryContract);
    }
}
