import { Component, OnInit } from '@angular/core';
import {LoggerService} from 'eds-angular4';
import {Router} from '@angular/router';
import {ConceptService} from './concept.service';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ConceptCreateComponent} from './concept-create/concept-create.component';
import {SearchResult} from '../models/SearchResult';
import {DocumentService} from '../document/document.service';
import {ModuleStateService} from 'eds-angular4/dist/common';
import {IMModel} from '../models/IMModel';

@Component({
  selector: 'app-concept-library',
  templateUrl: './concept-library.component.html',
  styleUrls: ['./concept-library.component.css']
})
export class ConceptLibraryComponent implements OnInit {
  listTitle = 'Most recently used';
  summaryList: SearchResult;
  searchTerm: string;
  models: IMModel[];
  modelSelection: number[];

  constructor(private router: Router,
              private modal: NgbModal,
              private conceptService: ConceptService,
              private log: LoggerService,
              private state: ModuleStateService
  ) { }

  ngOnInit() {
    let s = this.state.getState('ConceptLibrary');
    if (s) {
      this.searchTerm = s.searchTerm;
      this.modelSelection = s.modelSelection;
    }
    this.search();
    this.getModels();
    this.getCodeSchemes();
  }

  getMRU() {
    this.summaryList = null;
    this.conceptService.getMRU()
      .subscribe(
        (result) => this.summaryList = result,
        (error) => this.log.error(error)
      );
  }

  getModels() {
    this.models = [];
    this.conceptService.getModels()
      .subscribe(
        (result) => this.models = result,
        (error) => this.log.error(error)
      );
  }

  getCodeSchemes() {
    // this.conceptService.getSubtypes(5300, true) // 5300 = Code scheme supertype
    //   .subscribe(
    //     (result) => this.codeSchemes = result,
    //     (error) => this.log.error(error)
    //   )
  }

  search(page: number = 1) {
    if (this.searchTerm) {
      // if (this.summaryList == null || this.summaryList.page != page) {
      this.listTitle = 'Search results for "' + this.searchTerm + '"';
      this.summaryList = null;
      this.conceptService.search({term: this.searchTerm, size: 15, page: page, models: this.modelSelection})
        .subscribe(
          (result) => this.summaryList = result,
          (error) => this.log.error(error)
        );
      // }
    }
    else
      this.getMRU();
  }

  clear() {
    this.listTitle = 'Most recently used';
    this.searchTerm = '';
    this.getMRU();
  }

  editConcept(concept: any) {
    this.state.setState('ConceptLibrary', {searchTerm: this.searchTerm, modelSelection: this.modelSelection});
    this.router.navigate(['concept', concept.id])
  }

  addConcept() {
    ConceptCreateComponent.open(this.modal)
      .result.then(
      (result) => this.router.navigate(['concept', result])
    );

  }

  gotoPage(page) {
    this.listTitle = 'Search results for "' + this.searchTerm + '"';
    this.summaryList = null;
    this.conceptService.search({term: this.searchTerm})
      .subscribe(
        (result) => this.summaryList = result,
        (error) => this.log.error(error)
      );
  }
}
