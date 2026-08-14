//! Shared deterministic semantic messages used by M005 platform certification.

use athena_domain::{FolioId, ProjectId, TemplateSegment, TemplateText, VariableReference};
use athena_editor::{TitleBlockField, TitleBlockValue};

use crate::{AthenaMessage, DocumentMessage, PortfolioMessage};

/// Builds the canonical project/folio/title-block message sequence.
pub fn canonical_m005_messages(
    project_id: ProjectId,
    initial_folio_id: FolioId,
    control_folio_id: FolioId,
    io_folio_id: FolioId,
) -> Vec<AthenaMessage> {
    vec![
        PortfolioMessage::CreateProject {
            project_id,
            initial_folio_id,
            name: "Main Distribution".into(),
        }
        .into(),
        DocumentMessage::AddFolio {
            folio_id: control_folio_id,
            label: "Control".into(),
        }
        .into(),
        DocumentMessage::AddFolio {
            folio_id: io_folio_id,
            label: "I/O".into(),
        }
        .into(),
        DocumentMessage::RenameFolio {
            folio_id: control_folio_id,
            label: "Control".into(),
        }
        .into(),
        DocumentMessage::RenameFolio {
            folio_id: io_folio_id,
            label: "I/O".into(),
        }
        .into(),
        DocumentMessage::MoveFolio {
            folio_id: io_folio_id,
            to: 1,
        }
        .into(),
        DocumentMessage::ActivateFolio {
            folio_id: io_folio_id,
        }
        .into(),
        DocumentMessage::SetProjectVariable {
            key: "plant".into(),
            value: Some("PLANT-A".into()),
        }
        .into(),
        DocumentMessage::SetProjectVariable {
            key: "designer".into(),
            value: Some("A. Engineer".into()),
        }
        .into(),
        DocumentMessage::SetFolioVariable {
            folio_id: io_folio_id,
            key: "area".into(),
            value: Some("MCC-01".into()),
        }
        .into(),
        DocumentMessage::SetTitleBlockValue {
            folio_id: io_folio_id,
            field: TitleBlockField::Title,
            value: TitleBlockValue::Template(TemplateText::literal("Main control")),
        }
        .into(),
        DocumentMessage::SetTitleBlockValue {
            folio_id: io_folio_id,
            field: TitleBlockField::Author,
            value: TitleBlockValue::Template(TemplateText(vec![TemplateSegment::Variable(
                VariableReference::Project("designer".into()),
            )])),
        }
        .into(),
        DocumentMessage::SetTitleBlockValue {
            folio_id: io_folio_id,
            field: TitleBlockField::Location,
            value: TitleBlockValue::Template(TemplateText::literal("MCC-01")),
        }
        .into(),
        DocumentMessage::SetTitleBlockValue {
            folio_id: io_folio_id,
            field: TitleBlockField::Revision,
            value: TitleBlockValue::Template(TemplateText::literal("A")),
        }
        .into(),
        DocumentMessage::SetTitleBlockValue {
            folio_id: io_folio_id,
            field: TitleBlockField::PageNumber,
            value: TitleBlockValue::Template(TemplateText::literal("2")),
        }
        .into(),
        PortfolioMessage::RequestSave.into(),
    ]
}
