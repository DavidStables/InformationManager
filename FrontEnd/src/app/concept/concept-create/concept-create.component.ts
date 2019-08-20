import {AfterViewInit, Component, ElementRef, ViewChild} from '@angular/core';
import {NgbActiveModal, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ConceptService} from '../concept.service';
import {LoggerService} from 'eds-angular4';
import {Router} from '@angular/router';
import {IMDocument} from '../../models/IMDocument';
import {DocumentService} from '../../document/document.service';

@Component({
  selector: 'app-concept-create',
  templateUrl: './concept-create.component.html',
  styleUrls: ['./concept-create.component.css']
})
export class ConceptCreateComponent implements AfterViewInit {
  public static open(modal: NgbModal) {
    const modalRef = modal.open(ConceptCreateComponent, { backdrop: 'static'});
    return modalRef;
  }

  id : string;
  document: IMDocument;
  name: string;
  documents: IMDocument[] = [];


  constructor(private dialog: ElementRef,
              private modal: NgbModal,
              private activeModal: NgbActiveModal,
              private conceptService: ConceptService,
              private documentService: DocumentService,
              private logger: LoggerService,
              private router: Router) { }

  @ViewChild('focus') focusField: ElementRef;
  ngAfterViewInit(): void {
    this.documentService.getDocuments()
      .subscribe(
        (result) => this.documents = result,
        (error) => this.logger.error(error)
      );
    if (this.focusField != null)
      this.focusField.nativeElement.focus();
  }

  ok() {
    this.conceptService.getName(this.id)
      .subscribe(
        (result) => result == '' ? this.save() : this.exists(),
        (error) => this.logger.error(error)
      );
  }

  exists() {
    this.logger.error('A concept with this id already exists!', this.id, 'Error creating concept');
  };

  save() {
    this.documentService.createConcept(this.document.path, this.id, this.name)
      .subscribe(
        () => this.activeModal.close(this.id),
        (error) => this.logger.error(error)
      );
  }

  cancel() {
    this.activeModal.dismiss();
  }

}
