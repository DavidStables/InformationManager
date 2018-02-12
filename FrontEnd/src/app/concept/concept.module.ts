import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ConceptListComponent} from './concept-list/concept-list.component';
import {ConceptService} from './concept.service';
import {FormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {CoreModelModuleComponent} from './concept-modules/core-model-module.component';
import {RecordStructuresModuleComponent} from './concept-modules/record-structures-module.component';
import {CodeableConceptsModuleComponent} from './concept-modules/codeable-concepts-module.component';
import {ControlsModule} from 'eds-angular4/dist/controls';
import {ViewModuleComponent} from './concept-modules/view-module.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import { PickerDialogComponent } from './picker-dialog/picker-dialog.component';
import {EventRecordEditorComponent} from './event-record-editor/event-record-editor.component';
import {AbstractFieldEditorComponent} from './abstract-field-editor/abstract-field-editor.component';
import {FieldEditorComponent} from './field-editor/field-editor.component';
import {CardinalityDialogComponent} from './cardinality-dialog/cardinality-dialog.component';
import {ViewEditorComponent} from './view-editor/view-editor.component';
import {BaseConceptEditorComponent} from './base-concept-editor/base-concept-editor.component';
import { ViewNavigationDialogComponent } from './view-navigation-dialog/view-navigation-dialog.component';

@NgModule({
  imports: [
    BrowserModule,
    CommonModule,
    FormsModule,
    NgbModule,
    ControlsModule
  ],
  declarations: [
    BaseConceptEditorComponent,
    ConceptListComponent,
    CoreModelModuleComponent,
    RecordStructuresModuleComponent,
    CodeableConceptsModuleComponent,
    ViewModuleComponent,
    EventRecordEditorComponent,
    AbstractFieldEditorComponent,
    FieldEditorComponent,
    ViewEditorComponent,

    PickerDialogComponent,
    CardinalityDialogComponent,
    ViewNavigationDialogComponent
  ],
  providers: [ConceptService],
  entryComponents: [
    PickerDialogComponent,
    CardinalityDialogComponent,
    ViewNavigationDialogComponent
  ]

})
export class ConceptModule { }
