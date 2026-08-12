use athena_domain::{SymbolDefinition, SymbolDefinitionId};

use crate::{SymbolCatalog, SymbolDefinitionRecord};

/// A case-insensitive text query over symbol names and tags.
#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct SearchQuery {
    text: String,
}

impl SearchQuery {
    #[must_use]
    pub fn new(text: impl AsRef<str>) -> Self {
        Self {
            text: text.as_ref().trim().to_lowercase(),
        }
    }

    #[must_use]
    pub fn is_empty(&self) -> bool {
        self.text.is_empty()
    }
}

/// A deterministic catalog search result.
#[derive(Clone, Copy, Debug)]
pub struct SearchResult<'a> {
    pub definition: &'a SymbolDefinition,
    record: &'a SymbolDefinitionRecord,
}

impl SymbolCatalog {
    #[must_use]
    pub fn search(&self, query: &SearchQuery) -> Vec<SearchResult<'_>> {
        let mut matches = self
            .records()
            .filter(|record| matches_query(record, query))
            .map(|record| SearchResult {
                definition: &record.definition,
                record,
            })
            .collect::<Vec<_>>();

        matches.sort_by(|left, right| {
            let left_name = left.definition.name.to_lowercase();
            let right_name = right.definition.name.to_lowercase();

            left_name
                .cmp(&right_name)
                .then_with(|| left.definition.name.cmp(&right.definition.name))
                .then_with(|| definition_id(left.record).cmp(&definition_id(right.record)))
        });
        matches
    }
}

fn matches_query(record: &SymbolDefinitionRecord, query: &SearchQuery) -> bool {
    query.is_empty()
        || record
            .definition
            .name
            .to_lowercase()
            .contains(query.text.as_str())
        || record
            .definition
            .tags
            .iter()
            .any(|tag| tag.to_lowercase().contains(query.text.as_str()))
}

fn definition_id(record: &SymbolDefinitionRecord) -> SymbolDefinitionId {
    record.definition.id
}
