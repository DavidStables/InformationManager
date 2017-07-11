import { Injectable } from '@angular/core';
import {ConceptSummary} from "./models/concept-summary";
import {URLSearchParams, Http} from "@angular/http";
import {Observable} from "rxjs/Observable";
import {ConceptRelationship} from "./models/concept-relationship";

@Injectable()
export class ConceptModellerService {
  private CONCEPTS : ConceptSummary[];

  constructor(private http : Http) {
    this.CONCEPTS = [
      { id: 1, name: 'Observation', clazz: 1, /*count: 2, */description: '', shortName: ''},
      { id: 2, name: 'Person', clazz: 1, /*count: 9, */description: '', shortName: ''},
      { id: 3, name: 'Encounter', clazz: 1, /*count: 23,*/ description: '', shortName: ''},
      { id: 4, name: 'Diabetes', clazz: 1, /*count: 42, */description: '', shortName: ''},
      { id: 5, name: 'Diabetes Mellitus', clazz: 1, /*count: 31, */description: '', shortName: ''},
      { id: 6, name: 'Type I Diabetes', clazz: 1, /*count: 3, */description: '', shortName: ''},
      { id: 7, name: 'Type II Diabetes', clazz: 1,/* count: 5, */description: '', shortName: ''},
      { id: 8, name: 'Gender', clazz: 1, /*count: 88, */description: '', shortName: ''},
      { id: 9, name: 'Male', clazz: 1, /*count: 32, */description: '', shortName: ''},
      { id: 10, name: 'Female', clazz: 1, /*count: 53, */description: '', shortName: ''},
      { id: 11, name: 'Penicillin Allergy', clazz: 1, /*count: 21, */description: '', shortName: ''},
      { id: 12, name: 'Asthma', clazz: 1, /*count: 32, */description: '', shortName: ''},
      { id: 13, name: 'Co-codamol', clazz: 1, /*count: 11, */description: '', shortName: ''},
      { id: 14, name: 'Paracetamol', clazz: 1, /*count: 23, */description: '', shortName: ''},
      { id: 15, name: 'Influenza Vaccination', clazz: 1, /*count: 1, */description: '', shortName: ''}
    ];
  }

  public findConceptsByName(search: string, pageNumber: number = 1, pageSize: number = 10): Observable<ConceptSummary[]> {
    let vm = this;
    let params = new URLSearchParams();
    params.set('conceptName',search);
    params.set('pageNumber', pageNumber.toString());
    params.set('pageSize', pageSize.toString());
    return vm.http.get('/api/informationModel', {withCredentials : true, search : params} )
      .map((response) => response.json());
  }

  public getConceptSearchTotalCount(search: string): Observable<number> {
    const vm = this;
    const params = new URLSearchParams();
    params.set('conceptName', search);
    return vm.http.get('/api/informationModel/searchCount', {withCredentials : true, search : params} )
      .map((response) => response.json());
  }

  public findConceptsById(id: number): Observable<ConceptSummary> {
    let vm = this;
    let params = new URLSearchParams();
    params.set('conceptId', id.toString());
    return vm.http.get('/api/informationModel', {withCredentials : true, search : params} )
      .map((response) => response.json());
  }

  public getCommonConcepts(limit : number) : Observable<ConceptSummary[]> {
    let vm = this;
    let params = new URLSearchParams();
    params.set('limit','10');
    console.log('getting common');
    return vm.http.get('/api/informationModel/common', {withCredentials : true, search : params} )
      .map((response) => response.json());
  }

  /*Code added to simply test the API calls...feel free to delete*/

  public saveConcept(concept : ConceptSummary) : Observable<any> {
    let vm = this;
    return vm.http.post('/api/informationModel', concept, {withCredentials : true})
      .map((response) => response.json());
  }

  public populateAllConcepts(): void {
    let vm = this;

    for (let concept of vm.CONCEPTS) {
      vm.saveConcept(concept)
        .subscribe(
          (result) => console.log(result),
          (error) => console.log(error)
        );
    }
  }

  public deleteConcept(conceptId: number): Observable<any> {
    let vm = this;

    let params = new URLSearchParams();
    params.set('conceptId', conceptId.toString());
    return vm.http.delete('/api/informationModel', {withCredentials : true, search : params})
      .map((response) => response.toString());
  }

  public deleteConceptRelationship(conceptRelationshipId: number): Observable<any> {
    let vm = this;

    let params = new URLSearchParams();
    params.set('conceptRelationshipId', conceptRelationshipId.toString());
    return vm.http.delete('/api/informationModel/relationships', {withCredentials : true, search : params})
      .map((response) => response.toString());
  }

  public addRelationship(conceptRelationship: ConceptRelationship): Observable<any> {
    let vm = this;

    return vm.http.post('/api/informationModel/relationships', conceptRelationship)
      .map((response) => response.toString());
  }

  public getRelationships(conceptId: number): Observable<ConceptRelationship[]> {
    let vm = this;

    let params = new URLSearchParams();
    params.set('conceptId', conceptId.toString());
    return vm.http.get('/api/informationModel/relationships', {withCredentials : true, search : params})
      .map((response) => response.json());
  }

  public getRelationshipTypes(): Observable<ConceptSummary[]> {
    let vm = this;

    return vm.http.get('/api/informationModel/relationshipConcepts', {withCredentials : true})
      .map((response) => response.json());
  }

  public getClasses(): Observable<ConceptSummary[]> {
    let vm = this;

    return vm.http.get('/api/informationModel/classConcepts', {withCredentials : true})
      .map((response) => response.json());
  }
}
