//! Scoped custom variables and deterministic template resolution.

use std::collections::BTreeMap;

use serde::{Deserialize, Serialize};

use crate::{TemplateSegment, TemplateText, VariableReference};

/// Diagnostic emitted when a typed variable reference cannot be resolved.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct VariableDiagnostic {
    /// The unresolved typed reference.
    pub reference: VariableReference,
}

/// Rendered text plus non-destructive resolution diagnostics.
#[derive(Clone, Debug, Default, Deserialize, Eq, PartialEq, Serialize)]
pub struct ResolvedTemplateText {
    /// Renderable text with unresolved references kept visible.
    pub text: String,
    /// Typed diagnostics for every unresolved reference.
    pub diagnostics: Vec<VariableDiagnostic>,
}

/// Resolves typed segments against explicit project and folio variable maps.
#[must_use]
pub fn resolve_template_text(
    template: &TemplateText,
    project_variables: &BTreeMap<String, String>,
    folio_variables: &BTreeMap<String, String>,
) -> ResolvedTemplateText {
    let mut resolved = ResolvedTemplateText::default();
    for segment in &template.0 {
        match segment {
            TemplateSegment::Literal(value) => resolved.text.push_str(value),
            TemplateSegment::Variable(reference) => {
                let value = match reference {
                    VariableReference::Project(name) => project_variables.get(name),
                    VariableReference::Folio(name) => folio_variables.get(name),
                };
                if let Some(value) = value {
                    resolved.text.push_str(value);
                } else {
                    resolved.text.push_str(&unresolved_text(reference));
                    resolved.diagnostics.push(VariableDiagnostic {
                        reference: reference.clone(),
                    });
                }
            }
        }
    }
    resolved
}

pub(crate) fn normalize_variable_key(key: &str) -> Option<String> {
    let normalized = key.trim();
    if normalized.is_empty() || normalized.chars().any(char::is_control) {
        None
    } else {
        Some(normalized.to_owned())
    }
}

fn unresolved_text(reference: &VariableReference) -> String {
    match reference {
        VariableReference::Project(name) => format!("{{Project:{name}}}"),
        VariableReference::Folio(name) => format!("{{Folio:{name}}}"),
    }
}
