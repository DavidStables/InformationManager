import {Status} from './Status';
import {Version} from './Version';

export class Concept {
  dbid : number;
  document : number;
  id: string;
  data : any;
  status : Status;
  updated : Date;
  revision : number;
  published : Version;
}
