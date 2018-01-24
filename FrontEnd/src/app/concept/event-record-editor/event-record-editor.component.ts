import { Component } from '@angular/core';
import {ConceptRelationship} from '../../models/concept-relationship';
import {Relationship} from '../../models/relationship';
import {Class} from '../../models/class';
import {PickerDialogComponent} from '../picker-dialog/picker-dialog.component';
import {BaseConceptEditorComponent} from '../base-concept-editor/base-concept-editor.component';
import {Concept} from '../../models/concept';
import {InputBoxDialog} from 'eds-angular4';
import {CardinalityDialogComponent} from '../cardinality-dialog/cardinality-dialog.component';

@Component({
  selector: 'app-event-record-editor',
  templateUrl: './event-record-editor.component.html',
  styleUrls: ['./event-record-editor.component.css']
})
export class EventRecordEditorComponent extends BaseConceptEditorComponent {
  Class = Class;  // Import enum for template use
  Relationship = Relationship;

  private fields: ConceptRelationship[] = null;

  createNew() {
    this.setConcept(
      {
        name: 'New Event/Record Concept',
        clazz: Class.EVENT_TYPE.getId(),
        status: 0
      } as Concept
    );
  }

  addField() {
    const vm = this;
    PickerDialogComponent.open(this.modal, 'Select field', [Class.FIELD, Class.FIELD_LIBRARY])
      .result.then(
      (result) => vm.processSelectedField(result)
    );
  }

  processSelectedField(field: Concept) {
    if (!field)
      return;

    const vm = this;
    if (field.clazz == Class.FIELD_LIBRARY.getId()) {
      field.id = null;  // Create new from this
      field.name = field.name + ' (' + vm.concept.name + ')';
      InputBoxDialog.open(vm.modal, 'Create Field', 'This will immediately create a new field concept, based on the selected abstract field, with the following name', field.name)
        .result.then(
        () => vm.createFieldFromAbstract(field)
      );
    } else {
      vm.createRelationship(field);
    }
  }

  createFieldFromAbstract(abstractField: Concept) {
    // Create new field in DB
    this.createRelationship(abstractField);
  }

  createRelationship(field: Concept) {
    let relationship: ConceptRelationship = {
      id: null,
      sourceId: this.concept.id,
      sourceName: this.concept.name,
      targetId: field.id,
      targetName: field.name,
      relationshipId: Relationship.FIELD,
      relationshipName: 'a field',
      cardinality: 1,
      order: this.related.length
    };

    this.addRelationship(relationship);
  }

  getFields(): ConceptRelationship[] {
    this.fields = [];
    for(let r of this.related) {
      if (r.relationshipId === Relationship.FIELD)
        this.fields.push(r);
    }

    return this.fields;
  }

  selectInheritance() {
    const vm = this;
    PickerDialogComponent.open(this.modal, 'Select parent concept', [Class.EVENT_TYPE, Class.RECORD_TYPE])
      .result.then(
      (result) => vm.setInheritance(result)
    );
  }

  setInheritance(parentConcept: Concept) {
    this.set_IS_Relationship(parentConcept, Relationship.FIELD_INHERITOR, 'field inheritor');
  }

  save() {
    const vm = this;
    if (vm.isValid()) {
      vm.conceptService.save(vm.concept, this.related)
        .subscribe(
          (result) => vm.savedOk(),
          (error) => vm.logger.error(error)
        );
    }
  }

  private isValid(): boolean {
    if (this.concept.clazz == Class.EVENT_TYPE.getId() && this.get_IS_RelationshipSingle(Relationship.FIELD_INHERITOR) == null) {
      this.logger.error('Event types must have a field inheritor');
      return false;
    }

    return true;
  }

  private savedOk() {
    this.logger.success(this.concept.name + ' saved', null, 'Save');
    this.location.back();
  }

  deleteField(row: ConceptRelationship) {
    this.removeRelationship(row);
  }

  editField(row: ConceptRelationship) {
    const vm = this;
    CardinalityDialogComponent.open(vm.modal, row.cardinality)
      .result.then(
      (result) => row.cardinality = result
    );
  }
}
