import { Component, OnInit } from '@angular/core';
import {ConceptSummary} from "../models/concept-summary";
import {ConceptModellerService} from "../concept-modeller.service";

@Component({
  selector: 'app-count-reports',
  templateUrl: './concept-modeller.component.html',
  styleUrls: ['./concept-modeller.component.css']
})
export class ConceptModellerComponent implements OnInit {
  summaryList : ConceptSummary[];
  searchTerms : string;

  constructor(private conceptService : ConceptModellerService) {
    let vm = this;
    vm.conceptService.getCommonConcepts(10)
      .subscribe(
        (result) => vm.summaryList = result,
        (error) => console.log(error)
    );
  }

  ngOnInit() {
  }

  findConcepts() {
    let vm = this;
    vm.conceptService.findConceptsByName(vm.searchTerms)
      .subscribe(
        (result) => vm.summaryList = result,
        (error) => console.log(error)
      );
  }

  populateDB() {
    let vm = this;
    vm.conceptService.populateAllConcepts();
  }
}
