import { Injectable } from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Concept} from '../models/Concept';
import {ConceptTreeNode} from '../models/ConceptTreeNode';
import {SearchResult} from '../models/SearchResult';
import {Namespace} from '../models/Namespace';
import {Axiom} from '../models/Axiom';
import {ConceptDefinition} from '../models/ConceptDefinition';
import {SimpleConcept} from '../models/definitionTypes/SimpleConcept';
import {PropertyDefinition} from '../models/definitionTypes/PropertyDefinition';
import {PropertyRange} from '../models/definitionTypes/PropertyRange';

@Injectable({
  providedIn: 'root'
})
export class ConceptService {
  private _nameCache: any = {};

  constructor(private http: HttpClient) { }

  getMRU(args: {size?: number, supertypes?: string[]}): Observable<SearchResult> {
    let params = new HttpParams();

    if (args.size) params = params.append('size', args.size.toString());
    if (args.supertypes) {
      for (let item of args.supertypes)
        params = params.append('supertype', item);
    }

    return this.http.get<SearchResult>('api/concepts/mru', {params});
  }

  getNamespaces(): Observable<Namespace[]> {
    return this.http.get<Namespace[]>('api/namespaces');
  }

  getConcept(iri: string): Observable<Concept> {
    return this.http.get<Concept>('api/concepts/' + iri);
  }

  getAxioms(): Observable<Axiom[]> {
    return this.http.get<Axiom[]>('api/concepts/axioms');
  }

  getConceptDefinition(iri: string): Observable<ConceptDefinition> {
    return this.http.get<ConceptDefinition>('api/concepts/' + iri + '/definition');
  }

  search(args: {term: string, supertypes?: string[], size?: number, page?: number, models?: string[], status?: string[]}): Observable<SearchResult> {
    let params = new HttpParams();
    params = params.append('term', args.term);

    if (args.supertypes) {
      for (let item of args.supertypes)
      params = params.append('supertype', item);
    }
    if (args.size) params = params.append('size', args.size.toString());
    if (args.page) params = params.append('page', args.page.toString());
    if (args.models) {
      for (let item of args.models)
        params = params.append('model', item);
    }
    if (args.status) {
      for (let item of args.status)
        params = params.append('status', item);
    }


    return this.http.get<SearchResult>('api/concepts/search', {params});
  }

  complete(args: {term: string, superTypeFilter?: string[]}): Observable<Concept[]> {
    let params = new HttpParams();
    params = params.append('term', args.term);

    if (args.superTypeFilter) {
      for (let item of args.superTypeFilter)
        params = params.append('superType', item);
    }

    return this.http.get<Concept[]>('api/concepts/complete', {params});
  }

  completeWord(term: string): Observable<string> {
    let params = new HttpParams();
    params = params.append('term', term);
    return this.http.get('api/concepts/completeWord', {params: params, responseType: 'text'});
  }

  getName(conceptId: string) {
    if (!this._nameCache[conceptId]) {

      this._nameCache[conceptId] = conceptId;

      this.http.get('api/concepts/' + conceptId + '/name', {responseType: 'text'})
        .subscribe(
          (response) => this._nameCache[conceptId] = ( response || conceptId),
          (error) => console.error(error)
        );
    }
    return this._nameCache[conceptId];
  }

  getParentTree(conceptId: string, root?: string): Observable<ConceptTreeNode[]> {
    let params = new HttpParams();

    if (root) params = params.append('root', root);

    return this.http.get<ConceptTreeNode[]>('api/concepts/' + conceptId + '/parentTree', {params: params});
  }

  getParents(conceptId: string): Observable<Concept[]> {
    return this.http.get<Concept[]>('api/concepts/' + conceptId + '/parents');
  }

  getChildren(conceptId: string): Observable<Concept[]> {
    return this.http.get<Concept[]>('api/concepts/' + conceptId + '/children');
  }

  getRootConcepts(): Observable<Concept[]> {
    return this.http.get<Concept[]>('api/concepts/root');
  }

  getConceptHierarchy(conceptId: any): Observable<ConceptTreeNode[]> {
    return this.http.get<ConceptTreeNode[]>('api/concepts/' + conceptId + '/hierarchy')
  }

  getDescendents(conceptIri: string): Observable<Concept[]> {
    return this.getChildren(conceptIri);
  }

  createConcept(concept: Concept): Observable<string> {
    return this.http.post<string>('api/concepts', concept, { responseType: 'text' as 'json'});
  }

  updateConcept(concept: Concept) {
    return this.http.put('api/concepts', concept);
  }

  getAncestors(conceptIri: string): Observable<Concept[]> {
    return this.http.get<Concept[]>('api/concepts/' + conceptIri + '/ancestors');
  }


  addAxiomSupertype(conceptIri: string, axiom: Axiom, definition: SimpleConcept) {
    return this.http.post('api/concepts/' + conceptIri + '/' + axiom.token + '/supertypes', definition);
  }

  addAxiomRoleGroupProperty(conceptIri: string, axiom: Axiom, definition: PropertyDefinition, group: number=0) {
    return this.http.post('api/concepts/' + conceptIri + '/' + axiom.token + '/rolegroups/' + group , definition);
  }

  deleteAxiomSupertype(conceptIri: string, axiom: Axiom, supertype: string) {
    return this.http.delete('api/concepts/' + conceptIri + '/' + axiom.token + '/supertypes/' + supertype);
  }

  deleteAxiomRoleGroupProperty(conceptIri: string, axiom: Axiom, definition: PropertyDefinition, group: number) {
    if (definition.object)
      return this.http.delete('api/concepts/' + conceptIri + '/' + axiom.token + '/rolegroups/' + group + '/' + definition.property + '/object/' + definition.object);
    else
      return this.http.delete('api/concepts/' + conceptIri + '/' + axiom.token + '/rolegroups/' + group + '/' + definition.property + '/data/' + definition.data);
  }

  deleteAxiomRoleGroup(conceptIri: string, axiom: Axiom, group: number) {
    return this.http.delete('api/concepts/' + conceptIri + '/' + axiom.token + '/rolegroups/' + group);
  }

  deleteAxiom(conceptIri: string, axiom: Axiom) {
    return this.http.delete('api/concepts/' + conceptIri + '/' + axiom.token);
  }

  deleteConcept(conceptIri: string) {
    return this.http.delete('api/concepts/' + conceptIri);
  }

  getOperators(): Observable<string[]> {
    return this.http.get<string[]>('api/concepts/operators');
  }

  addPropertyRange(conceptIri: string, propertyRange: PropertyRange) {
    return this.http.post('api/concepts/' + conceptIri + '/propertyrange', propertyRange);
  }

  delPropertyRange(conceptIri: string, propertyRange: PropertyRange) {
    return this.http.delete('api/concepts/' + conceptIri + '/propertyrange/' + propertyRange.range);
  }
}
