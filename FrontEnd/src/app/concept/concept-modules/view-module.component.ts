import {BaseConceptModuleComponent} from './base-concept-module.component';
import {Concept} from '../../models/concept';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Component} from '@angular/core';
import {Class} from '../../models/class';

@Component({
  templateUrl: './base-concept-module.component.html',
  styleUrls: ['./base-concept-module.component.css']
})
export class ViewModuleComponent extends BaseConceptModuleComponent {
  constructor(modal: NgbModal) {
    super(modal, 'Views', 'fa-eye', [Class.FOLDER], true);
  }

  onConceptSelected(concept: Concept) {
    console.log('Selected VIEW FOLDER ' + concept.name);
  }

  addRecord() {
    console.log('Adding a new VIEW');
  }
}
