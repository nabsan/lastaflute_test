#!/usr/local/bin/perl
use strict;

#use lib qw{/usr/local/Cellar/perl/5.26.1/lib/perl5/site_perl/5.26.1/};
use warnings;
use utf8;
use Encode;
use JSON;

print "#stt\n";
binmode STDIN,  ':encoding(utf8)';
binmode STDOUT, ':encoding(utf8)';
binmode STDERR, ':encoding(utf8)';

my %hash = (
  'チョウ' => { 
    'アゲハチョウ'=>'Papilio xuthus', 
    'モンシロチョウ'=>'Pieris rapae', 
    'ゴマダラチョウ'=>'Hestina persimilis japonica' 
    },
  'コガネムシ' => { 
    'マメコガネ'=>'Popillia japonica', 
    'カナブン'=>'Rhomborrhina　japonica', 
    'スジコガネ'=>'Anomala testaceipes' 
    },
  'バッタ' => { 
    'トノサマバッタ'=>'Locusta migratoria', 
    'ショウリョウバッタ'=>'Acrida cinerea', 
    'コバネイナゴ'=>'Oxya yezoensis' 
    }
  );

my $hash_ref = \%hash;
my $hash_json = JSON->new->encode($hash_ref);

print $hash_json;
print "\n";

use DBI;

# PostgreSQL
our $DB_NAME = "hoge";
our $DB_USER = "ma";
our $DB_PASS = "00x";
our $DB_HOST = "127.0.0.1";
our $DB_PORT = "5432";

my $dbh = DBI->connect("dbi:Pg:dbname=$DB_NAME;host=$DB_HOST;port=$DB_PORT","$DB_USER","$DB_PASS") or die "$!\n Error: failed to connect to DB.\n";
my $sth = $dbh->prepare("SELECT * from foos ");
$sth->execute();
while (my $ary_ref = $sth->fetchrow_arrayref) {
  my ($col1,$col2,$col3) = @$ary_ref;
  print $col1,"\n",$col2,"\n",$col3 , "\n";
}
$sth->finish;
#
#$sth = $dbh->prepare("insert into foos values('otiai1', '{\"spam\":\"0\", \"ham\": [3,5,8,22] }','{\"toto\": \"fuga\",\"oppai\":true}');");
$sth = $dbh->prepare("insert into foos values('otiai2', '{\"spam\":\"0\", \"ham\": [3,5,8,22] }','".$hash_json."');");
my $res=$sth->execute();
$sth->finish;
$dbh->disconnect;

print "#end $res\n";
